import sys

class Match(object):
    def __init__(self, left_player, right_player, left_pq, right_pq, left_score, right_score):
        self.left_player = left_player
        self.right_player = right_player

        self.d = left_pq[1] + left_pq[0]

        self.left_diff = abs(left_pq[1] - left_pq[0])
        self.right_diff = abs(right_pq[1] - right_pq[0])

        if self.d != right_pq[1] + right_pq[0]:
            print 'No consistant d: {} vs {}'.format(left_pq, right_pq)
        
        self.left_score = left_score
        self.right_score = right_score

        if left_score == right_score and left_score == 0:
            self.complete = False

            self.winner = None
            self.loser = None
        else:
            self.complete = True

            self.winner = self.left_player if left_score > right_score else self.right_player
            self.loser  = self.left_player if left_score < right_score else self.right_player

    def diff(self, player):
        if player == self.left_player:
            return self.left_diff
        elif player ==  self.right_player:
            return self.right_diff

    def margin(self, player):
        if player ==  self.left_player:
            return self.left_score - self.right_score
        elif player ==  self.right_player:
            return self.right_score - self.left_score

    def __contains__(self, player):
        return self.left_player == player or self.right_player == player

    def __str__(self):
        if self.complete:
            return "{} went first, and {} {}, {} - {}.".format(self.left_player,
                    "beat" if self.winner == self.left_player else "lost to",
                    self.right_player,
                    self.left_score,
                    self.right_score)
        else:
            return "{} played {}, but it didn't terminate.".format(self.left_player, self.right_player)

def parse_match(match_str):
    if len(match_str.strip().split('\t')) != 9:
        print "Failed to parse '{}'".format(match_str)
        return None
        
    left_player, right_player, left_p, left_q, right_p, right_q, ticks, left_score, right_score = match_str.strip().split('\t')

    left_pq = (int(left_p), int(left_q))
    right_pq = (int(right_p), int(right_q))

    left_score = int(left_score)
    right_score = int(right_score)

    return Match(left_player, right_player, left_pq, right_pq, left_score, right_score)

def parse_match_file(f):
    return map(parse_match, f)

def histogram(data, key_fn):
    result = {}
    for d in data:
        key = key_fn(d)
        if key not in result:
            result[key] = []
        result[key].append(d)

    return result


def count_wins(matches, p):
    return len([m for m in matches if p in m and m.winner == p])

def count_losses(matches, p):
    return len([m for m in matches if p in m and m.loser == p])

def stuff(matches, p):
    wins   = len([m for m in matches if p in m and m.winner == p])
    losses = len([m for m in matches if p in m and m.loser == p])
    draws  = len([m for m in matches if p in m and m.complete == False])

    average_margin_of_victory  = sum([m.margin(p) for m in matches if p in m and m.winner == p]) / wins
    average_margin_of_defeat   = sum([m.margin(p) for m in matches if p in m and m.loser == p]) / losses

    print "{}: {} - {} - {} - {} - {}".format(p, wins, losses, draws, average_margin_of_victory, average_margin_of_defeat)

def matches_by_d(p, matches):
    return matches_by_key(p, matches, lambda match: match.d)

def matches_by_key(p, matches, key_fn):
    matches_with_winners = [m for m in matches if m.winner is not None]

    games_by_d = histogram(matches_with_winners, key_fn)

    for k, v in games_by_d.iteritems():
        wins = count_wins(v, p)
        losses = count_losses(v, p)

        if losses == 0:
            losses = 1

        print "{}\t{}\t{}\t{}".format(k, float(wins)/losses, wins, losses)

def percent_of_winners_who(matches, condition):
    matches_with_winners = [m for m in matches if m.winner is not None]
    matching_condition = filter(condition, matches_with_winners)
    return float(len(matching_condition)) / len(matches_with_winners), matching_condition, matches_with_winners

if __name__ == "__main__":
    with open(sys.argv[1]) as f:
        matches = parse_match_file(f)
    players = set([m.left_player for m in matches])
                
    us = 'group6'

    matches_by_d('group6', matches)

    #matches_with_winners = [m for m in matches if m.winner is not None]

    #matches_with_winners_who_went_first, _, _ = percent_of_winners_who(matches, lambda match: match.winner == match.left_player)
    #print "Percentage of winners that went first: {}".format(matches_with_winners_who_went_first)

    #matches_with_winners_with_better_diff, _, _ = percent_of_winners_who(matches, lambda match: match.diff(match.winner) <= match.diff(match.loser))
    #print "Percentage of winners with a better pq diff: {}".format(matches_with_winners_with_better_diff)
