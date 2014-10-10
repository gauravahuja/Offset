#!/usr/bin/env python

from collections import namedtuple
import re
from subprocess import Popen, PIPE
import math
import sys
import os

from multiprocessing.pool import ThreadPool

DEBUG = False
VERBOSE = False

MAX_THREADS = 4

Player = namedtuple('Player', ['name', 'p', 'q'])
Player.d = lambda self: self.p + self.q
Player.diff = lambda self: self.q - self.p

game_regex = re.compile(r"Winner: \w+. Final score: (?P<left>-?\d+) - (?P<right>-?\d+)")
assert(game_regex.match("Winner: dumb. Final score: 490 - 522").groupdict() == {'left': '490', 'right':'522'})

class GameFailedException(Exception):
    pass

def run_match(left, right):
    def parse_match_output(output_lines):
        matches = game_regex.match(output_lines[-1])
        if matches is None:
            raise GameFailedException("Game output didn't match expectations: '{}'".format(output_lines[-1]))

        scores = map(int, matches.groups())
        return scores

    assert(left.d() == right.d())
    command = '''java offset.sim.AltOffset {d} {left.name} {right.name} 0 /dev/null {left.p} {left.q} {right.p} {right.q}'''.format(
        d = left.d(),
        left = left,
        right = right)

    if DEBUG:
        print command+"\n"

    out, err = Popen(command.split(' '), stdout=PIPE, stderr=PIPE).communicate()

    output_lines = err.strip().split('\n')
    if VERBOSE:
        print output_lines[-1]
    return parse_match_output(output_lines)

def differential(scores):
    if scores[0] == -1:
        return -scores[1]
    if scores[1] == -1:
        return scores[0]
    return scores[0] - scores[1]

def run_all_matches(left_name, right_name, d):
    all_pqs = [(p, d - p) for p in range(1,d-1) if p < d - p]
    pq_pairs = [(pq1, pq2) for pq1 in all_pqs for pq2 in all_pqs if pq1 != pq2 and (pq1[0] != pq2[1])]

    if VERBOSE:
        print "Running {} matches...".format(len(pq_pairs))

    side = int(math.sqrt(len(pq_pairs) + d/2))
    thread_pool = ThreadPool(MAX_THREADS)

    def run_pair(tup):
        i = tup[0]
        pair = tup[1]
        reruns = tup[2]

        left_player = Player(left_name, *pair[0])
        right_player = Player(right_name, *pair[1])
        scores = (0, 0)
        result = 0

        try:
            scores = run_match(left_player, right_player)
            result = differential(scores)
        except GameFailedException:
            if DEBUG:
                print "Game Exception"
            if reruns > 0:
                return run_pair((i, pair, reruns - 1))

        if DEBUG:
            print "Finished match {}: result: {}. Left player: {} - {}. Right player: {} - {}.".format(
                    i + 1, result, left_player.p, left_player.q,
                    right_player.p, right_player.q)

        return pair, result, scores

    MAX_RERUNS = 3
    pair_results = thread_pool.map(run_pair, [(i, pair, MAX_RERUNS) for i, pair in enumerate(pq_pairs)])

    final_results = [[None for i in range(side)] for j in range(side)]
    def printCSVFormat(left_player, right_player, pq0, pq1, scores):
        assert(sum(pq0) == sum(pq1))
        #player0, player1, d, p0, q0, p1, q1, winner, score0, score1
        winner = ""
        if (scores[0] > scores[1]):
            winner = left_player
        elif (scores[1] > scores[0]):
            winner = right_player
        else:
            winner = 'Tie'
        print >> sys.stderr, '{0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}'.format(
            left_player,
            right_player,
            sum(pq0),
            pq0[0],
            pq0[1],
            pq1[0],
            pq1[1],
            winner,
            scores[0],
            scores[1])



    for result in pair_results:
        pairs = result[0]
        row = (pairs[0][1] - pairs[0][0] - 1) / 2
        col = (pairs[1][1] - pairs[1][0] - 1) / 2

        final_results[row][col] = result[1]
        scores = result[2]
        printCSVFormat(left_name, right_name, pairs[0], pairs[1], scores)

    return final_results

right_color = "\033[34m"
left_color  = "\033[32m"

def print_heatmap(left_player, right_player, d, results):
    top_col_spacing = " " * (len(left_player) + 2 + 6)
    left_col_spacing = " " * (len(left_player) + 2)
    print top_col_spacing + "{}\033[1m{}\033[0m".format(right_color, right_player)

    def val(d, i):
        if d % 2 == 0:
            return i * 2
        else:
            return i * 2 + 1

    val_format = "{:>6}"
    def val_string(d, i):
        return val_format.format("+{}".format(val(d,i)))

    def write_header():
        sys.stdout.write(top_col_spacing)
        for i in range(len(results[0])):
            sys.stdout.write("{:>+6d}".format(val(d,i)))
        print

    def score_string(value):
        if value is None:
            return val_format.format("")

        color = right_color if value < 0 else left_color
        mod = "\033[1m" if abs(value) > 150 else ""
        return "{}{}{}\033[0m".format(color, mod, val_format.format(value))

    def write_row(i, row):
        if i == 0:
            sys.stdout.write(" {} {}".format("{}\033[1m{}\033[0m".format(left_color, left_player), val_string(d,i)))
        else:
            sys.stdout.write("{}{}".format(left_col_spacing, val_string(d,i)))

        for value in row:
            sys.stdout.write(score_string(value))

        print

    write_header();
    for i, row in enumerate(results):
        write_row(i, row)

    print

def path_to_player(player_name):
    return os.path.join('.', 'offset', player_name, 'Player.java')

def player_exists(player_name):
    return os.path.isfile(path_to_player(player_name)) and player_name != 'sim'

def all_players():
    return [player for player in os.listdir('./offset') if player_exists(player)]


def main():
    if len(sys.argv) == 1:
        print "Players:"
        for player in all_players():
            print "\t{}".format(player)
        return
    elif len(sys.argv) < 4:
        print "Forgot to specify d."
        print "Usage: ./{} <player> <opponent1> [<opponent2> ...] <d>"
        return 1

    left_player  = sys.argv[1]
    opponents = sys.argv[2:-1]

    if not os.path.isfile(os.path.join('.', 'offset', left_player, 'Player.java')):
        print "Player with name {} not found.".format(left_player)
        sys.exit(1)

    if any([o == 'all' for o in opponents]):
        opponents = all_players()


    for opponent in opponents:
        if not player_exists(opponent):
            print "Player with name {} not found.".format(opponent)
            sys.exit(1)

    DEVNULL = open('/dev/null', 'w')
    out, err = Popen(['make', 'compile'], stdout=DEVNULL, stderr=DEVNULL).communicate()

    d = int(sys.argv[-1])

    for opponent in opponents:
        if VERBOSE:
            print 'Playing {} against {}.'.format(left_player, opponent)
        results = run_all_matches(left_player, opponent, d)
        print_heatmap(left_player, opponent, d, results)


if __name__ == '__main__':
    main()
