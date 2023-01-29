import re
import signal
import subprocess
import sys
from argparse import ArgumentParser
from datetime import datetime
from multiprocessing import Pool, Value
from pathlib import Path
from typing import Any

def run_matches(player1: str, player2: str, maps: list[str], timestamp: str) -> dict[str, Any]:
    result = {
        "player1": player1,
        "player2": player2
    }

    winners_by_map = {}
    current_map = None

    args = [
        str(Path(__file__).parent.parent / "gradlew"),
        "run",
        f"-PteamA={player1}",
        f"-PteamB={player2}",
        f"-Pmaps={','.join(maps)}",
        f"-PreplayPath=replays/run-{timestamp}-%TEAM_A%-vs-%TEAM_B%.bc23"
    ]

    proc = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

    lines = []
    while True:
        line = proc.stdout.readline()
        if not line:
            break

        line = line.decode("utf-8").rstrip()
        lines.append(line)

        map_match = re.search(r"[^ ]+ vs\. [^ ]+ on ([^ ]+)", line)
        if map_match is not None:
            current_map = map_match[1]

        result_match = re.search(r"([^ ]+) \([AB]\) wins \(round (\d+)\)", line)
        if result_match is not None:
            global counter
            with counter.get_lock():
                counter.value += 1
                current_match = counter.value

            total_matches = len(maps) * 2
            prefix = f"[{str(current_match).rjust(len(str(total_matches)))}/{total_matches}]"

            winner_color = "red" if result_match[1] == player1 else "blue"

            print(f"{prefix} {result_match[1]} wins in {result_match[2]} rounds as {winner_color} on {current_map}")
            winners_by_map[current_map] = result_match[1]

    if proc.wait() != 0:
        result["type"] = "error"
        result["message"] = "\n".join(lines)
        return result

    result["type"] = "success"
    result["winners"] = winners_by_map
    return result

def main() -> None:
    parser = ArgumentParser(description="Compare the performance of two players.")
    parser.add_argument("player1", help="name of the first player")
    parser.add_argument("player2", help="name of the second player")

    args = parser.parse_args()

    signal.signal(signal.SIGINT, lambda a, b: sys.exit(1))

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

        # Sprint 2
        "BattleSuns",
        "Checkmate2",
        "Cornucopia",
        "Crossword",
        "Cube",
        "Divergence",
        "FourNations",
        "HideAndSeek",
        "Lantern",
        "Lines",
        "Maze",
        "Pakbot",
        "Piglets",
        "Risk",
        "Sine",
        "Snowflake",
        "SomethingFishy",
        "Spin",
        "Spiral",
        "Squares",
        "Star",
        "Sus",
        "SweetDreams",
        "TicTacToe",
        "USA",

        # International Qualifiers
        "Barcode",
        "Contraction",
        "Flower",
        "Grapes",
        "IslandHopping",
        "Marsh",
        "RaceToTheTop",
        "Repetition",
        "River",
        "RockWall",
        "Sakura",
        "SoundWave",
        "ThirtyFive",
        "TimesUp",
        "TreasureMap",
    ]

    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")

    global counter
    counter = Value("i", 0)

    print(f"Running {len(maps) * 2} matches")

    with Pool(2) as pool:
        results = pool.starmap(run_matches, [(args.player1, args.player2, maps, timestamp),
                                             (args.player2, args.player1, maps, timestamp)])

    if any(r["type"] == "error" for r in results):
        for r in results:
            if r["type"] == "error":
                print(f"{r['player1']} versus {r['player2']} failed with the following error:")
                print(r["message"])
        sys.exit(1)

    map_winners = {}

    player1_wins = 0
    player2_wins = 0

    for r in results:
        for map, winner in r["winners"].items():
            if map in map_winners and map_winners[map] != winner:
                map_winners[map] = "Tied"
            else:
                map_winners[map] = winner

            if winner == args.player1:
                player1_wins += 1
            else:
                player2_wins += 1

    tied_maps = [k for k, v in map_winners.items() if v == "Tied"]
    player1_superior_maps = [k for k, v in map_winners.items() if v == args.player1]
    player2_superior_maps = [k for k, v in map_winners.items() if v == args.player2]

    if len(tied_maps) > 0:
        print(f"Tied maps ({len(tied_maps)}):")
        for map in tied_maps:
            print(f"- {map}")
    else:
        print(f"There are no tied maps")

    if len(player1_superior_maps) > 0:
        print(f"Maps {args.player1} wins on as both red and blue ({len(player1_superior_maps)}):")
        for map in player1_superior_maps:
            print(f"- {map}")
    else:
        print(f"There are no maps {args.player1} wins on as both red and blue")

    if len(player2_superior_maps) > 0:
        print(f"Maps {args.player2} wins on as both red and blue ({len(player2_superior_maps)}):")
        for map in player2_superior_maps:
            print(f"- {map}")
    else:
        print(f"There are no maps {args.player2} wins on as both red and blue")

    print(f"{args.player1} wins: {player1_wins} ({player1_wins / (player1_wins + player2_wins) * 100:,.2f}% win rate)")
    print(f"{args.player2} wins: {player2_wins} ({player2_wins / (player1_wins + player2_wins) * 100:,.2f}% win rate)")

if __name__ == "__main__":
    main()
