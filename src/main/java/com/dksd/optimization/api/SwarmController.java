package com.dksd.optimization.api;

import com.dksd.optimization.FitnessFunction;
import com.dksd.optimization.StandardConcurrentSwarm;
import com.dksd.optimization.Swarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * Controls the adding and execution of optimization problem definitions.
 *
 * @author dscottdawkins
 */
@RestController(value = "/problems")
public class SwarmController {

    private Logger logger = LoggerFactory.getLogger(getClass());
    //private EncryptDecrypt encryptDecrypt;
    private Map<ProblemDefinition, Swarm> problemDefinitions = new ConcurrentHashMap<>();
    private Map<Swarm, Long> iterationsPerSecond = new HashMap<>();
    private Set<Swarm> runningSwarms = Collections.synchronizedSet(new HashSet<>());
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    /** Ctor. */
    public SwarmController() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                logger.info("Running steps for number of swarms: {}", runningSwarms.size());
                Set<Swarm> copiedRunningSwarms = new HashSet<>(runningSwarms);
                for (Swarm copiedRunningSwarm : copiedRunningSwarms) {
                    long iterations = 1;
                    if (iterationsPerSecond.get(copiedRunningSwarm) != null) {
                        iterations = iterationsPerSecond.get(copiedRunningSwarm);
                    }
                    for (long i = 0; i < iterations; i++) {
                        long start = System.currentTimeMillis();
                        try {
                            if (copiedRunningSwarm.step()) {
                                runningSwarms.remove(copiedRunningSwarm);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (iterationsPerSecond.get(copiedRunningSwarm) == null) {
                            long end = System.currentTimeMillis();
                            iterations = 1000 / (end - start + 1);
                            if (iterations > 1000) {
                                iterations = 1000;
                            }
                            iterationsPerSecond.put(copiedRunningSwarm, iterations);
                        }
                    }
                }
            }
        };
        executorService.scheduleWithFixedDelay(runnable, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Adds a new problem definition to the list of currently registered problems with the REST service.
     *
     * @param entry the problem definition to add
     * @return whether the problem was successfully created and added to the current list of problems
     */
    @PostMapping(value = "/problems")
    public ResponseEntity<String> addProblem(@RequestBody ProblemDefinition entry) {
        problemDefinitions.put(entry, createSwarm(entry));
        logger.info("Added swarm {}", entry.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Starts or stops the execution of a problem definition.
     *
     * @param name the name of the problem to perform the action on
     * @param action the action to perform, 'Start' or 'Stop' executing.
     * @return Ok if the action was executed successfully
     */
    @PutMapping(value = "/problems/{name}/{action}")
    public ResponseEntity<String> startStopProblem(@PathVariable String name, @PathVariable Action action) {
        for (ProblemDefinition problemDefinition : problemDefinitions.keySet()) {
            logger.info("Comparing names: {} and {}", name, problemDefinition.getName());
            if (problemDefinition.getName().equals(name)) {
                if (Action.START.equals(action)) {
                    logger.info("Starting swarm {}", problemDefinition.getName());
                    runningSwarms.add(problemDefinitions.get(problemDefinition));
                } else if (Action.STOP.equals(action)) {
                    logger.info("Stopping swarm {}", problemDefinition.getName());
                    runningSwarms.remove(problemDefinitions.get(problemDefinition));
                }
            }
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Get information about this controller and service.
     *
     * @return Ok if all problem definitions were reported
     */
    @GetMapping(value = "/problems")
    public ResponseEntity<Collection<String>> getAllProblems() {
        Collection<String> problems = new ArrayList<>();
        for (Map.Entry<ProblemDefinition, Swarm> problem : problemDefinitions.entrySet()) {
            problems.add(problem.getKey().getName() + " Gbest Fitness: " + problem.getValue().getGbestFitness() +
            " Gbest: " + problem.getValue().getGbest());
        }
        return new ResponseEntity<>(problems, HttpStatus.OK);
    }

    @GetMapping(value = "/info")
    public String getInfo() {
        StringBuilder sb = new StringBuilder("Particle Swarm Optimization Service as a REST service. \n");
        return sb.toString();
    }

    private synchronized Swarm createSwarm(ProblemDefinition entry) {
        try {
            //String decryptedCode = encryptDecrypt.decrypt(entry.getFitnessFunctionCode());
            FitnessFunction ff = createFitnessFunction(entry.getFitnessFunctionCode(), entry.getFitnessFunctionClassName());
            return new StandardConcurrentSwarm(ff, entry.getNumParticles());
        } catch (Throwable ep) {
            logger.error("Could not create swarm {}.", entry.getName(), ep);
        }
        throw new IllegalArgumentException("Could not create swarm!");
    }

    private FitnessFunction createFitnessFunction(String fitnessCode, String className) throws Exception {
        return createClass(compileSource(saveSource(fitnessCode, className), className), className);
    }

    private Path saveSource(String source, String className) throws IOException {
        String tmpProperty = System.getProperty("java.io.tmpdir");
        Path sourcePath = Paths.get(tmpProperty, className+ ".java");
        Files.write(sourcePath, source.getBytes(UTF_8));
        return sourcePath;
    }

    private Path compileSource(Path javaFile, String className) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, javaFile.toFile().getAbsolutePath());
        return javaFile.getParent().resolve(className + ".class");
    }

    private FitnessFunction createClass(Path javaClass, String className)
            throws MalformedURLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        URL classUrl = javaClass.getParent().toFile().toURI().toURL();
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classUrl});
        Class<?> clazz = Class.forName(className, true, classLoader);
        return (FitnessFunction) clazz.newInstance();
    }
}



/*
try {
            encryptDecrypt = new EncryptDecrypt();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
 */