/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dksd.optimization;

import com.dksd.optimization.api.ProblemDefinition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Standard simple test of the endpoint.
 *
 * @author dscottdawkins
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EntryControlTests {

    @Autowired
    private MockMvc mockMvc;

    private Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

    @Test
    public void addEntryTest() throws Exception {
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

        String exampleEntry = gson.toJson(entry);
        this.mockMvc.perform(post("/problems").
                contentType(MediaType.APPLICATION_JSON)
                .content(exampleEntry))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    public void getListOfProblemsTest() throws Exception {
        addEntryTest();
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/problems").
                contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        Assert.assertTrue(response.contains("Test Swarm Gbest Fitness"));
    }
}
