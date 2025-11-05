# PageRank Parallel Processing

## Compilation
To compile the program, run:
```bash
make all
```
This will generate an executable named `pagerank`.

## Usage
Run the program using the following command:
```bash
time ./pagerank input/<filename> 50 <num_of_threads>
```
where:
- `<filename>` is the input graph file.
- `50` is the number of PageRank iterations. **[I realized last minute that the number of iterations is fixed at 50]**
- `<num_of_threads>` specifies the number of threads to use.

## Example
```bash
time ./pagerank input/Email-Enron.txt 50 4
```
This runs PageRank on `Email-Enron.txt` for **50 iterations** using **4 threads**, while measuring execution time.

## Notes
- Ensure your input file follows the correct format (edges as space-separated pairs of integers).
- For benchmarking, run the program multiple times and compute average execution time.

## Cleaning Up
To remove compiled files, run:
```bash
make clean
```

