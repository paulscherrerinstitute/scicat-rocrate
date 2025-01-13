/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package org.psi.scicat;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.PrintUtil;

/**
 *
 * @author zade_o
 */
public class EquivalenceDemo {

    public static void main(String[] args) {

        String resourcePath = "scicat-merged-export.ttl"; // Relative to resources folder
        ClassLoader classLoader = EquivalenceDemo.class.getClassLoader();
        java.net.URL resourceURL = classLoader.getResource(resourcePath);

        // In this case, the Turtle file contains both the schema and the instance, hence using an empty model
        Model model = ModelFactory.createDefaultModel();
        Model schema = RDFDataMgr.loadModel(resourceURL.toString());
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        reasoner = reasoner.bindSchema(schema);

        InfModel infmodel = ModelFactory.createInfModel(reasoner, model);

        Property dataciteCreator = infmodel.getProperty("http://datacite.org/schema/kernel-4#creatorName");

        System.out.println("Subject | Predicate | Object:");
        printStatements(infmodel, null, dataciteCreator, null);

    }

    public static void printStatements(Model m, Resource s, Property p, Resource o) {
        for (StmtIterator i = m.listStatements(s, p, o); i.hasNext();) {
            Statement stmt = i.nextStatement();
            System.out.println(" - " + PrintUtil.print(stmt));
        }
    }

}
