#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <omp.h>


int count_live_neighbors(char **grid, int rows, int cols, int i, int j) {
    int count = 0;
    for (int di = -1; di <= 1; di++) {
        for (int dj = -1; dj <= 1; dj++) {
            if (di == 0 && dj == 0) continue; 
            int ni = i + di;
            int nj = j + dj;
            /*Boundry chekc*/
            if (ni >= 0 && ni < rows && nj >= 0 && nj < cols) {
                if (grid[ni][nj] == '*') {
                    count++;
                }
            }
        }
    }
    return count;
}

void simulate_generation(char **current, char **next, int rows, int cols) {
    #pragma omp parallel for collapse(2) schedule(static)
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            int live_neighbors = count_live_neighbors(current, rows, cols, i, j);
            /*Conway's rules*/ 
            if (current[i][j] == '*') {
                if (live_neighbors < 2 || live_neighbors > 3) {
                    next[i][j] = ' '; /*Dies*/
                } else {
                    next[i][j] = '*'; /*Survives*/ 
                }
            } else {
                if (live_neighbors == 3) {
                    next[i][j] = '*'; /*Becomes alive*/ 
                } else {
                    next[i][j] = ' '; /*Stays dead*/ 
                }
            }
        }
    }
}

void read_grid(FILE *file, char **grid, int rows, int cols) {
    char line[2 * cols + 2]; /*giving stractuer to ||*/
    fgets(line, sizeof(line), file); 
    for (int i = 0; i < rows; i++) {
        if (fgets(line, sizeof(line), file) == NULL) {
            fprintf(stderr, "Error reading grid row %d\n", i);
            exit(1);
        }
        for (int j = 0; j < cols; j++) {
            grid[i][j] = line[1 + 2*j];
        }
    }
}

void write_grid(FILE *file, char **grid, int rows, int cols) {
    fprintf(file, "%d %d\n", rows, cols);
    for (int i = 0; i < rows; i++) {
        fprintf(file, "|");
        for (int j = 0; j < cols; j++) {
            fprintf(file, "%c|", grid[i][j]);
        }
        fprintf(file, "\n");
    }
}

int main(int argc, char *argv[]) {
    if (argc != 5) {
        fprintf(stderr, "Usage: %s <input_file> <generations> <output_file> <num_threads>\n", argv[0]);
        return 1;
    }

    int num_threads = atoi(argv[4]);
    omp_set_num_threads(num_threads);

    /*Open file */
    FILE *input = fopen(argv[1], "r");
    if (input == NULL) {
        perror("Error opening input file");
        return 1;
    }

    /*Gets dimensions */
    int rows, cols;
    if (fscanf(input, "%d %d", &rows, &cols) != 2) {
        fprintf(stderr, "Error reading dimensions from input file\n");
        fclose(input);
        return 1;
    }
    char **current = (char **)malloc(rows * sizeof(char *));
    char **next = (char **)malloc(rows * sizeof(char *));
    for (int i = 0; i < rows; i++) {
        current[i] = (char *)malloc(cols * sizeof(char));
        next[i] = (char *)malloc(cols * sizeof(char));
    }
    read_grid(input, current, rows, cols);
    fclose(input);

    /*Numbs of gens*/
    int generations = atoi(argv[2]);
    if (generations < 0) {
        fprintf(stderr, "Number of generations must be non-negative\n");
        return 1;
    }

    for (int gen = 0; gen < generations; gen++) {
        simulate_generation(current, next, rows, cols);
        char **temp = current;
        current = next;
        next = temp;
    }

    /*Generate output file with the final grid */
    FILE *output = fopen(argv[3], "w");
    if (output == NULL) {
        perror("Error opening output file");
        return 1;
    }
    write_grid(output, current, rows, cols);
    fclose(output);
    /*Free memory*/
    for (int i = 0; i < rows; i++) {
        free(current[i]);
        free(next[i]);
    }
    free(current);
    free(next);

    return 0;
}