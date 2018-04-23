package com.dksd.optimization;

import com.dksd.optimization.api.ProblemDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple JSON encoding and decoding test.
 *
 * @author dscottdawkins
 */
@RunWith(SpringRunner.class)
@JsonTest
public class EntryJsonTest {

    @Autowired
    private JacksonTester<ProblemDefinition> json;

    @Test
    public void testSerialize() throws Exception {
        ProblemDefinition entry = new ProblemDefinition();
        entry.setName("Test Swarm");
        entry.setDescription("This swarm is to test out the initial implementation and will be a parabola");
        entry.setFitnessFunctionClassName("FitnessFunctionTest");
        entry.setNumParticles(3);
        entry.setFitnessFunctionCode("import com.dksd.optimization.FitnessFunction;\n" +
                "import com.dksd.optimization.Particle;\n" +
                "\n" +
                "public class FitnessFunctionTest implements FitnessFunction {\n" +
                "\n" +
                "    public double calcFitness(Particle p) {\n" +
                "        return p.getGene().getValue(0) * p.getGene().getValue(0);\n" +
                "    }\n" +
                "\n" +
                "    public int getDimension() {\n" +
                "        return 1;\n" +
                "    }\n" +
                "\n" +
                "    public double[] getDomain() {\n" +
                "        double[] domain = new double[getDimension()];\n" +
                "        for (int i = 0; i < domain.length; i++) {\n" +
                "            domain[i] = 1000;\n" +
                "        }\n" +
                "        return domain;\n" +
                "    }\n" +
                "}\n");

        // Assert against a `.json` file in the same package as the test
        //assertThat(this.json.write(entry)).isEqualToJson("expected.json");
        // Or use JSON path based assertions
        assertThat(this.json.write(entry)).hasJsonPathStringValue("@.name");
        assertThat(this.json.write(entry)).extractingJsonPathStringValue("@.name")
                .isEqualTo("Test Swarm");
        assertThat(this.json.write(entry)).hasJsonPathStringValue("@.description");
        assertThat(this.json.write(entry)).extractingJsonPathStringValue("@.description")
                .isEqualTo("This swarm is to test out the initial implementation and will be a parabola");

        assertThat(this.json.write(entry)).hasJsonPathStringValue("@.fitnessFunctionClassName");
        assertThat(this.json.write(entry)).extractingJsonPathStringValue("@.fitnessFunctionClassName")
                .isEqualTo("FitnessFunctionTest");

        assertThat(this.json.write(entry)).hasJsonPathNumberValue("@.numParticles");
        assertThat(this.json.write(entry)).extractingJsonPathNumberValue("@.numParticles")
                .isEqualTo(3);

        System.out.println(this.json.write(entry));
    }

    @Test
    public void testDeserialize() throws Exception {
        String input = "{\"name\":\"Test Swarm\",\"description\":\"This swarm is to test out the initial implementation and will be a parabola\",\"numParticles\":3,\"fitnessFunctionCode\":\"import com.dksd.optimization.FitnessFunction;\\nimport com.dksd.optimization.Particle;\\n\\npublic class FitnessFunctionTest implements FitnessFunction {\\n\\n    public double calcFitness(Particle p) {\\n        return p.getGene().getValue(0) * p.getGene().getValue(0);\\n    }\\n\\n    public int getDimension() {\\n        return 1;\\n    }\\n\\n    public double[] getDomain() {\\n        double[] domain = new double[getDimension()];\\n        for (int i = 0; i < domain.length; i++) {\\n            domain[i] = 1000;\\n        }\\n        return domain;\\n    }\\n}\\n\",\"fitnessFunctionClassName\":\"FitnessFunctionTest\"}";
        this.json.parse(input);
    }
}
