import asyncio
import asyncio.subprocess
import math
import signal
import sys
import traceback
import websockets
from argparse import ArgumentParser
from battlecode.schema.Event import Event
from battlecode.schema.EventWrapper import EventWrapper
from battlecode.schema.MatchFooter import MatchFooter
from battlecode.schema.MatchHeader import MatchHeader
from battlecode.schema.Round import Round
from dataclasses import dataclass
from datetime import datetime
from enum import StrEnum
from pathlib import Path
from rich.console import Console
from rich.table import Table

class Outcome(StrEnum):
    MATCH_END = "match end"
    MAP_CONTROL = "map control"

@dataclass
class Match:
    player1: str
    player2: str
    map: str
    winner: str
    rounds: int
    outcome: Outcome

    def __str__(self) -> str:
        winner_color = "red" if self.winner == self.player1 else "blue"
        return f"{self.winner} wins in {self.rounds} rounds as {winner_color} on {self.map} (win by {self.outcome})"

@dataclass
class State:
    player1: str
    player2: str
    maps: list[str]
    matches: list[Match]
    console: Console

processes: list[asyncio.subprocess.Process] = []

def cleanup(exit_code: int) -> None:
    global processes
    for proc in processes:
        if proc.returncode is None:
            proc.kill()

    sys.exit(exit_code)

def print_results(state: State) -> None:
    table = Table()

    column_groups = 4
    for _ in range(column_groups):
        for name in ["Map", "As red", "As blue"]:
            table.add_column(name)

    row_count = math.ceil(len(state.maps) / column_groups)
    for y in range(row_count):
        row = []
        for x in range(column_groups):
            map_idx = x * row_count + y
            if map_idx >= len(state.maps):
                row.extend([""] * 3)
                continue

            map = state.maps[map_idx]
            row.append(map)

            for p1, p2 in [[state.player1, state.player2], [state.player2, state.player1]]:
                match = next((m for m in state.matches if m.map == map and m.player1 == p1 and m.player2 == p2), None)
                if match is None:
                    row.append("")
                else:
                    if match.winner == state.player1:
                        row.append("✅")
                    else:
                        row.append("❌")

        table.add_row(*row)

    state.console.print(table)

    player1_wins = 0
    player2_wins = 0

    for match in state.matches:
        if match.winner == state.player1:
            player1_wins += 1
        else:
            player2_wins += 1

    if player1_wins > player2_wins:
        color = "green"
    elif player1_wins < player2_wins:
        color = "red"
    else:
        color = None

    prefix = f"[{color}]" if color is not None else ""
    suffix = "[/]" if color is not None else ""

    for name, my_wins, opponent_wins in [[state.player1, player1_wins, player2_wins], [state.player2, player2_wins, player1_wins]]:
        total_wins = my_wins + opponent_wins
        win_rate = my_wins / total_wins if total_wins > 0 else 0

        state.console.print(f"{prefix}{name} wins: [b]{my_wins}[/] ([b]{win_rate * 100:,.2f}%[/] win rate){suffix}")

async def run_match(player1: str, player2: str, map: str, timestamp: str, match_index: int, state: State) -> None:
    server_port = 6175 + 1 + match_index

    program = str(Path(__file__).parent.parent / "gradlew")
    args = [
        "run",
        "-x", "unpackClient",
        "-x", "checkForUpdates",
        f"-PteamA={player1}",
        f"-PteamB={player2}",
        f"-Pmaps={map}",
        f"-PreplayPath=replays/run-{timestamp}-{player1}-vs-{player2}-on-{map}.bc23",
        f"-PserverPort={server_port}"
    ]

    proc = await asyncio.subprocess.create_subprocess_exec(program,
                                                           *args,
                                                           stdout=asyncio.subprocess.PIPE,
                                                           stderr=asyncio.subprocess.STDOUT)

    global processes
    processes.append(proc)

    server_url = f"ws://localhost:{server_port}"

    while True:
        try:
            async with websockets.connect(server_url):
                break
        except:
            await asyncio.sleep(0.1)
            if proc.returncode is not None:
                break

    if proc.returncode is not None:
        state.console.print(f"{player1} versus {player2} failed on {map} with exit code {proc.returncode}")
        cleanup(1)

    winner = None
    rounds = None
    outcome = None

    async with websockets.connect(server_url) as websocket:
        hq_count = 0
        robots1 = set()
        robots2 = set()

        current_map_control_player = None
        map_control_streak = 0

        async for message in websocket:
            event = EventWrapper.GetRootAsEventWrapper(message)
            if event.EType() == Event.MatchHeader:
                match_header = MatchHeader()
                match_header.Init(event.E().Bytes, event.E().Pos)

                bodies = match_header.Map().Bodies()
                for i in range(bodies.RobotIdsLength()):
                    team = bodies.TeamIds(i)
                    robot_id = bodies.RobotIds(i)
                    (robots1 if team == 1 else robots2).add(robot_id)

                hq_count = len(robots1)
            if event.EType() == Event.Round:
                round = Round()
                round.Init(event.E().Bytes, event.E().Pos)

                rounds = round.RoundId()

                bodies = round.SpawnedBodies()
                for i in range(bodies.RobotIdsLength()):
                    team = bodies.TeamIds(i)
                    robot_id = bodies.RobotIds(i)
                    (robots1 if team == 1 else robots2).add(robot_id)

                for i in range(round.DiedIdsLength()):
                    robot_id = round.DiedIds(i)
                    robots1.discard(robot_id)
                    robots2.discard(robot_id)

                map_control_player = None
                if len(robots1) > hq_count * 5 and len(robots2) < hq_count * 2:
                    map_control_player = player1
                elif len(robots1) < hq_count * 2 and len(robots2) > hq_count * 5:
                    map_control_player = player2

                if map_control_player is not None and map_control_player == current_map_control_player:
                    map_control_streak += 1
                    if map_control_streak == 100:
                        winner = map_control_player
                        outcome = Outcome.MAP_CONTROL
                        proc.kill()
                        break
                else:
                    current_map_control_player = map_control_player
                    map_control_streak = 0
            elif event.EType() == Event.MatchFooter:
                match_footer = MatchFooter()
                match_footer.Init(event.E().Bytes, event.E().Pos)

                winner = player1 if match_footer.Winner() == 1 else player2
                outcome = Outcome.MATCH_END
                break

    if outcome != Outcome.MAP_CONTROL:
        exit_code = await proc.wait()
        if exit_code != 0:
            state.console.print(f"{player1} versus {player2} failed on {map} with exit code {exit_code}")
            cleanup(1)

    state.matches.append(Match(player1, player2, map, winner, rounds, outcome))
    print_results(state)

async def run_match_wrapper(semaphore: asyncio.Semaphore, *args) -> None:
    async with semaphore:
        return await run_match(*args)

async def run(player1: str, player2: str) -> None:
    # Based on SERVER_MAPS in https://github.com/battlecode/battlecode23/blob/master/client/visualizer/src/constants.ts
    maps = [
        # Default batch 1
        "AllElements",
        "DefaultMap",
        "maptestsmall",
        "SmallElements",

        # Default batch 2
        "Turtle",
        "Dreamy",
        "Forest",
        "PairedProgramming",
        "Rewind",

        # Sprint 1
        "ArtistRendition",
        "BatSignal",
        "BowAndArrow",
        "Cat",
        "Clown",
        "Diagonal",
        "Eyelands",
        "Frog",
        "Grievance",
        "Hah",
        "Jail",
        "KingdomRush",
        "Minefield",
        "Movepls",
        "Orbit",
        "Pathfind",
        "Pit",
        "Pizza",
        "Quiet",
        "Rectangle",
        "Scatter",
        "Sun",
        "Tacocat",
    ]

    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    semaphore = asyncio.Semaphore(4)

    console = Console(highlight=False)

    console.print(f"Running {2 * len(maps)} matches")

    tasks = []
    state = State(player1, player2, maps, [], console)

    print_results(state)

    for map_idx, map in enumerate(maps):
        for order_idx, players in enumerate([[player1, player2], [player2, player1]]):
            tasks.append(run_match_wrapper(semaphore, *players, map, timestamp, map_idx * 2 + order_idx, state))

    await asyncio.gather(*tasks)

async def main() -> None:
    parser = ArgumentParser(description="Compare the performance of two players.")
    parser.add_argument("player1", help="name of the first player")
    parser.add_argument("player2", help="name of the second player")

    args = parser.parse_args()

    signal.signal(signal.SIGINT, lambda a, b: cleanup(1))

    try:
        await run(args.player1, args.player2)
        cleanup(0)
    except Exception:
        traceback.print_exc()
        cleanup(1)

if __name__ == "__main__":
    asyncio.run(main())
