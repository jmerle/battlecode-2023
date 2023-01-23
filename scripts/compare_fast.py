import asyncio
import asyncio.subprocess
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

processes: list[asyncio.subprocess.Process] = []

def cleanup(exit_code: int) -> None:
    global processes
    for proc in processes:
        if proc.returncode is None:
            proc.kill()

    sys.exit(exit_code)

async def run_match(player1: str, player2: str, map: str, timestamp: str, match_index: int, match_count: int) -> Match:
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
        print(f"{player1} versus {player2} failed on {map} with exit code {proc.returncode}")
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
            print(f"{player1} versus {player2} failed on {map} with exit code {exit_code}")
            cleanup(1)

    match = Match(player1, player2, map, winner, rounds, outcome)

    prefix = f"[{str(match_index + 1).rjust(len(str(match_count)))}/{match_count}]"
    print(f"{prefix} {match}")

    return match

async def run_match_wrapper(semaphore: asyncio.Semaphore, *args) -> Match:
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

    print(f"Running {2 * len(maps)} matches")

    matches = []
    for map_idx, map in enumerate(maps):
        for order_idx, players in enumerate([[player1, player2], [player2, player1]]):
            matches.append(run_match_wrapper(semaphore, *players, map, timestamp, map_idx * 2 + order_idx, 2 * len(maps)))

    matches = await asyncio.gather(*matches)

    map_winners = {}

    player1_wins = 0
    player2_wins = 0

    for match in matches:
        if match.map in map_winners and map_winners[match.map] != match.winner:
            map_winners[match.map] = "Tied"
        else:
            map_winners[match.map] = match.winner

        if match.winner == player1:
            player1_wins += 1
        else:
            player2_wins += 1

    tied_maps = [k for k, v in map_winners.items() if v == "Tied"]
    player1_superior_maps = [k for k, v in map_winners.items() if v == player1]
    player2_superior_maps = [k for k, v in map_winners.items() if v == player2]

    if len(tied_maps) > 0:
        print(f"Tied maps ({len(tied_maps)}):")
        for map in tied_maps:
            print(f"- {map}")
    else:
        print(f"There are no tied maps")

    if len(player1_superior_maps) > 0:
        print(f"Maps {player1} wins on as both red and blue ({len(player1_superior_maps)}):")
        for map in player1_superior_maps:
            print(f"- {map}")
    else:
        print(f"There are no maps {player1} wins on as both red and blue")

    if len(player2_superior_maps) > 0:
        print(f"Maps {player2} wins on as both red and blue ({len(player2_superior_maps)}):")
        for map in player2_superior_maps:
            print(f"- {map}")
    else:
        print(f"There are no maps {player2} wins on as both red and blue")

    print(f"{player1} wins: {player1_wins} ({player1_wins / (player1_wins + player2_wins) * 100:,.2f}% win rate)")
    print(f"{player2} wins: {player2_wins} ({player2_wins / (player1_wins + player2_wins) * 100:,.2f}% win rate)")

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
