package web.scraping.scripts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class Util {

    public static LinkedHashMap<String, Integer> sortHashMapByValues(HashMap<String, Integer> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Integer> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues, Collections.reverseOrder());
        Collections.sort(mapKeys);

        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

        Iterator<Integer> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Integer values = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Integer comp1 = passedMap.get(key);
                Integer comp2 = values;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, values);
                    break;
                }
            }
        }

        return sortedMap;

        //Source: https://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
    }

    public static HashMap<String, Integer> loadSearchTerms(String filePath) {
        HashMap<String, Integer> searchTerms = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while((line = reader.readLine()) != null) {
                searchTerms.put(line, 0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return searchTerms;
    }

    public static String getFindTermRegex(String termsLine){
        List<String> improvedLineTerms = new ArrayList<>();

        for(String term : termsLine.toLowerCase().split("\\|")) {
            term = term.replaceAll("\\.", "\\\\.")
                       .replaceAll("\\+", "\\\\+")
                       .replaceAll("\\[", "\\\\[")
                       .replaceAll("\\]", "\\\\]")
                       .replaceAll("\\$", "\\\\$")
                       .replaceAll("\\^", "\\\\^")
                       .replaceAll("\\?", "\\\\?")
                       .replaceAll("\\(", "\\\\(")
                       .replaceAll("\\)", "\\\\)");

            if(term.contains("*")) {
                term = term.replaceAll("\\*", "");

                improvedLineTerms.add(" " + term + " ");

                improvedLineTerms.add(" " + term + ",");
                improvedLineTerms.add("," + term + ",");
                improvedLineTerms.add(";" + term + ",");
                improvedLineTerms.add("-" + term + ",");
                improvedLineTerms.add("/" + term + ",");
                improvedLineTerms.add("\\(" + term + ",");


                improvedLineTerms.add(" " + term + ";");
                improvedLineTerms.add("," + term + ";");
                improvedLineTerms.add(";" + term + ";");
                improvedLineTerms.add("-" + term + ";");
                improvedLineTerms.add("/" + term + ";");
                improvedLineTerms.add("\\(" + term + ";");

                improvedLineTerms.add(" " + term + "\\.");
                improvedLineTerms.add("," + term + "\\.");
                improvedLineTerms.add(";" + term + "\\.");
                improvedLineTerms.add("-" + term + "\\.");
                improvedLineTerms.add("/" + term + "\\.");
                improvedLineTerms.add("\\(" + term + "\\.");

                improvedLineTerms.add(" " + term + "\\(");
                improvedLineTerms.add("," + term + "\\(");
                improvedLineTerms.add(";" + term + "\\(");
                improvedLineTerms.add("-" + term + "\\(");
                improvedLineTerms.add("/" + term + "\\(");
                improvedLineTerms.add("\\(" + term + "\\(");

                improvedLineTerms.add(" " + term + "\\-");
                improvedLineTerms.add("," + term + "\\-");
                improvedLineTerms.add(";" + term + "\\-");
                improvedLineTerms.add("-" + term + "\\-");
                improvedLineTerms.add("/" + term + "\\-");
                improvedLineTerms.add("\\(" + term + "\\-");

                improvedLineTerms.add(" " + term + "/");
                improvedLineTerms.add("," + term + "/");
                improvedLineTerms.add(";" + term + "/");
                improvedLineTerms.add("-" + term + "/");
                improvedLineTerms.add("/" + term + "/");
                improvedLineTerms.add("\\(" + term + "/");
            }
            else {
                improvedLineTerms.add(term);
            }
        }

        StringBuilder improvedLine = new StringBuilder(improvedLineTerms.get(0));

        for(int i=1; i<improvedLineTerms.size(); i++) {
            improvedLine.append("|").append(improvedLineTerms.get(i));
        }

        return ".*(" + improvedLine.toString() + ").*";
    }
}
