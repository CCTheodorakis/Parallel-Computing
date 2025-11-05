#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <time.h>
#include <pthread.h>

#define MAX_NODES 50000

typedef struct node
{
    long vertex;
    struct node* next;
}Node;

typedef struct graph 
{
    Node** adjL;
    double* pagernk;
    long* numedge; /*how many edges a node has*/ 
    long numVer;
}Graph;

pthread_mutex_t mutex;  


typedef struct ThreadArgs {
    Graph* graph;
    int startIdx;
    int endIdx;
    double* new_pagernk;
} ThreadArgs;

Node *create_node(int v){
    Node *new_node = (Node*)malloc(sizeof(Node));
    new_node -> vertex = v;
    new_node -> next = NULL;
    return new_node; 
}

Graph *create_graph(int numVer){
    Graph * graph = (Graph*)malloc(sizeof(Graph));
    graph->numVer=numVer;
    graph->adjL = (Node**)malloc(numVer * sizeof(Node*));
    graph->pagernk = (double*)malloc(numVer * sizeof(double));
    graph->numedge = (long*)calloc(numVer, sizeof(long));
    for(int i = 0; i< numVer; i++){
        graph->adjL[i] = NULL; 
        graph->pagernk[i]=1.0;
    }
    return graph;
}

void* pagerank_calculate(void* arg) {
    ThreadArgs* args = (ThreadArgs*)arg;
    Graph* graph = args->graph;
    int startIdx = args->startIdx;
    int endIdx = args->endIdx;
    double* new_pagernk = args->new_pagernk;

    for (int j = startIdx; j <= endIdx; j++) {
        Node* temp = graph->adjL[j];
        double sum = 0.0;
        while (temp) {
            long neighbor = temp->vertex;
            if (graph->numedge[neighbor] > 0) {
                sum += 0.85 * (graph->pagernk[neighbor] / graph->numedge[neighbor]);
            }
            temp = temp->next;
        }
        new_pagernk[j] = 0.15 + sum; 
    }

    pthread_exit(NULL);
}


void pagerankAlgo(Graph* graph, int iterations, int num_threads)
{
    double* new_pagernk = (double*)malloc(graph->numVer * sizeof(double));
    pthread_t* threads = (pthread_t*)malloc(num_threads * sizeof(pthread_t));
    ThreadArgs* args = (ThreadArgs*)malloc(num_threads * sizeof(ThreadArgs));

    for (int i = 0; i < iterations; i++) {
        for (int j = 0; j < graph->numVer; j++) {
            new_pagernk[j] = 0.15; 
        }
        
        int range_size = graph->numVer / num_threads;
        for (int t = 0; t < num_threads; t++) {
            args[t].graph = graph;
            args[t].startIdx = t * range_size;
            args[t].endIdx = (t == num_threads - 1) ? graph->numVer - 1 : (t + 1) * range_size - 1;
            args[t].new_pagernk = new_pagernk;

            pthread_create(&threads[t], NULL, pagerank_calculate, (void*)&args[t]);
        }

        /*Waitng for threads to finish*/
        for (int t = 0; t < num_threads; t++) {
            pthread_join(threads[t], NULL);
        }

        double* temp = graph->pagernk;
        graph->pagernk = new_pagernk;
        new_pagernk = temp;
    }

    free(threads);
    free(args);
    free(new_pagernk);
}


void free_graph(Graph *graph){
    for (int i = 0; i < graph->numVer; i++){
        Node *curr = graph->adjL[i];
        while(curr != NULL ){
            Node *tmp = curr;
            curr = curr->next;
            free(tmp);
        }
    }
    free(graph->adjL);
    free(graph);
    
}


void add_edge(Graph *graph, long src, long dest){
    Node *new_node = create_node(dest);
    new_node->next = graph->adjL[src];
    graph->adjL[src] = new_node;
    graph->numedge[src]++;
}


Graph* ReadGraph(const char* file) {
    FILE* f = fopen(file, "r");
    if (!f) {
        perror("Error opening file");
        exit(EXIT_FAILURE);
    }

    long src, dest;
    bool visited[MAX_NODES] = {false};  
    long NodeCount = 0; /*unique node count*/
    char line[100];  

    /*finding unique nodes*/
    while (fgets(line, sizeof(line), f)) {
        if (line[0] == '#') continue;  

        if (sscanf(line, "%ld %ld", &src, &dest) == 2) {
            if (!visited[src]) {
                visited[src] = true;
                NodeCount++;
            }
            if (!visited[dest]) {
                visited[dest] = true;
                NodeCount++;
            }
        }
    }

    Graph* graph = create_graph(NodeCount);

    /*Adding edges */
    rewind(f);
    while (fgets(line, sizeof(line), f)) {
        if (line[0] == '#') continue;  

        if (sscanf(line, "%ld %ld", &src, &dest) == 2) {
            add_edge(graph, src, dest);
        }
    }

    fclose(f);
    return graph;
}

/*for debbuging*/
void print_graph(Graph* graph) {
    for (long i = 0; i < graph->numVer; i++) {
        printf("Node %ld:", i);
        Node* temp = graph->adjL[i];
        while (temp) {
            printf(" -> %ld", temp->vertex);
            temp = temp->next;
        }
        printf("\n");
    }
}

void pagerank_out(Graph* graph, const char* filename) {
    FILE* f = fopen(filename, "w");
    if (!f) {
        perror("Error opening output file");
        return;
    }

    fprintf(f, "node,pagerank\n");
    for (long i = 0; i < graph->numVer; i++) {
        fprintf(f, "%ld,%.6lf\n", i, graph->pagernk[i]);
    }

    fclose(f);
}



int main(int argc, char *argv[]) {
    if (argc < 4) {
        printf("Usage: %s <filename>\n", argv[0]);
        return EXIT_FAILURE;
    }
    
    int iterations = atoi(argv[2]);
    int num_threads = atoi(argv[3]);

    if (num_threads < 1 || num_threads > 4) {
        printf("Error: Number of threads must be between 1 and 4.\n");
        return EXIT_FAILURE;
    }

    Graph* graph = ReadGraph(argv[1]);

    pthread_mutex_init(&mutex, NULL);

    pagerankAlgo(graph, iterations, num_threads);
    pagerank_out(graph, "output/pagerank.csv");

    /*print_graph(graph);*/
    free_graph(graph);
    pthread_mutex_destroy(&mutex);
    return EXIT_SUCCESS;
}


