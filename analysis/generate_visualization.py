import pickle
import networkx as nx
import matplotlib.pyplot as plt

class GraphData:
    def __init__(self):
        self.p = None
        self.q = None
        self.g = None
        self.d_hist = None
        self.degree_spatial = None
        self.cc_spatial = None


def create_graph(p, q, size = 32):
    nodes = [(x, y) for x in xrange(0, 32) for y in xrange(0, 32)]
    g = nx.Graph()
    g.add_nodes_from(nodes)
    edges = []
    for x in xrange(0, 32):
        for y in xrange(0, 32):
            n = [0]*8
            n[0] = (x+p, y+q)
            n[1] = (x+p, y-q)
            n[2] = (x-p, y+q)
            n[3] = (x-p, y-q)
            n[4] = (x+q, y+p)
            n[5] = (x+q, y-p)
            n[6] = (x-q, y+p)
            n[7] = (x-q, y-p)
            for (xn, yn) in n:
                if (xn >= 0 and xn < size and yn >=0 and yn < size):
                    edge = ((x, y), (xn, yn))
                    edges.append(edge)
    g.add_edges_from(edges)
    return g      
            

def summarize(p, q, file_dir = "./", size=32):
    filename = "%s/%s_%s_%s" %(file_dir, str(p+q), str(p), str(q))
    s = "[d, p, q] = [%s, %s, %s]\n" %(str(p+q), str(p), str(q))

    g = create_graph(p, q, size)
    
    d_hist = nx.degree_histogram(g)
    d_hist_str = [str(i) for i in d_hist]
    s += ",".join(d_hist_str) + '\n'

    d = g.degree()
    degree_spatial = [[0 for x in xrange(0, size)] for y in xrange(0, size)]
    for (x, y) in d.keys():
        degree_spatial[y][x] = d[(x, y)]

    s += "Degree Spatial distribution\n"
    for y in xrange(0, size):
        s += "%s\n" %(degree_spatial[y])

    e = len(g.edges())
    m = 2*e
    s += "Edges: %d\nMoves: %d\n" %(e, m)

    max_clique_size = nx.algorithms.graph_clique_number(g)
    count_max_clique_size = nx.algorithms.graph_number_of_cliques(g)

    s += "Max Clique Size: %d\n" %(max_clique_size)
    s += "Number of Max Cliques: %d\n" %(count_max_clique_size)

    number_connected_components = nx.algorithms.number_connected_components(g)
    s += "Number of Connected Components: %d\n" %(number_connected_components)

    s += "Connected Components Spatial Distribution\n"
    cc_spatial = [[0 for x in xrange(0, size)] for y in xrange(0, size)]
    cc_count = 1
    for cc in nx.algorithms.connected_components(g):
        for (x, y) in cc:
            cc_spatial[y][x] = cc_count
        cc_count += 1

    r_just_size = len(str(cc_count))
    for y in xrange(0, size):
        row_string = []
        for x in xrange(0, size):
            temp_str = "%d" %(cc_spatial[y][x])
            temp_str = temp_str.rjust(r_just_size)
            row_string.append(temp_str)
        s += "[" + ",".join(row_string) +"]\n"

    gd = GraphData()
    gd.p = p
    gd.q = q
    gd.g = g
    gd.degree_spatial = degree_spatial
    gd.cc_spatial = cc_spatial

    f = open(filename+".txt", 'w')
    f.write(s)
    f.close()

    f = open(filename+".pickle", 'wb')
    pickle.dump(gd, f)
    f.close()




if __name__ == '__main__':
    pq_dict = {}
    for d in xrange(5, 33):
        for p in xrange(1,d):
            q = d - p
            if (p == q or q == 0 or p ==0):
                continue    
            pq_dict[(min(p, q), max(p, q))] = 1

    for (p, q) in pq_dict.keys():
        summarize(p, q, "results/")