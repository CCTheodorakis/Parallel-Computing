import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    private List<Body> bodies;
    private double universeSize;
    private ExecutorService executor;

    public Main(String inputFile) throws IOException {
        readInput(inputFile);
    }

    private void readInput(String inputFile) throws IOException {
        bodies = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        int n = Integer.parseInt(reader.readLine());
        universeSize = Double.parseDouble(reader.readLine());

        for (int i = 0; i < n; i++) {
            String[] parts = reader.readLine().split(" ");
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double vx = Double.parseDouble(parts[2]);
            double vy = Double.parseDouble(parts[3]);
            double mass = Double.parseDouble(parts[4]);
            String name = parts[5];
            bodies.add(new Body(x, y, vx, vy, mass, name));
        }
        reader.close();
    }

    public void simulate(int steps, int numThreads) {
        executor = Executors.newFixedThreadPool(numThreads);
        for (int step = 0; step < steps; step++) {
            /*Build new tree*/
            BHTree tree = new BHTree(new Quadrant(0, 0, universeSize * 2));
            for (Body b : bodies) {
                tree.insert(b);
            }

            List<Future<?>> futures = new ArrayList<>();
            for (Body b : bodies) {
                futures.add(executor.submit(() -> {
                    tree.calculateForce(b);
                }));
            }
            /* Wait for all threads*/
            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            for (Body b : bodies) {
                b.update(1.0); // Δt = 1 second
            }
        }
        executor.shutdown();
    }

    public void writeOutput(String outputFile) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
        writer.println(bodies.size());
        writer.println(universeSize);

        for (Body b : bodies) {
            writer.println(b.toString());
        }
        writer.close();
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java main <input_file> <output_file> <steps> [threads]");
            return;
        }
        String inputFile = args[0];
        String outputFile = args[1];
        int steps = Integer.parseInt(args[2]);
        int threads = args.length > 3 ? Integer.parseInt(args[3]) : Runtime.getRuntime().availableProcessors();

        try {
            Main simulator = new Main(inputFile);
            simulator.simulate(steps, threads);
            simulator.writeOutput(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}