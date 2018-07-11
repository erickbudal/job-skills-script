package web.scraping.scripts;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class CathoScript {
    private String inputFilePath;
    private String outputFilePath;
    private String searchKeyword;

    public CathoScript(String inputFilePath, String outputFilePath, String searchKeyword) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        this.searchKeyword = searchKeyword;
    }

    public void start() {

        HashMap<String, Integer> searchTerms;
        String jobsListURL = "https://www.catho.com.br/vagas/?q=%s&page=";
        int exceptions = 0;
        int jobCounter = 0;
        int currentJob = 1;
        int totalPages;

        jobsListURL = String.format(jobsListURL, searchKeyword.replaceAll(" ", "+"));

        searchTerms = Util.loadSearchTerms(inputFilePath);

        try {
            Document jobListPage = Jsoup.connect(jobsListURL + 1).get();
            Elements paginationTags = jobListPage.select("#resultado-da-sua-busca-de-vaga > nav").get(0).getElementsByTag("a");
            totalPages = Integer.parseInt(paginationTags.get(paginationTags.size() - 2).text());
        }
        catch (Exception ex){
            ex.printStackTrace();
            return;
        }

        for (int i = 1; i <= totalPages; i++) {
            try {
                System.out.println("Current page: " + i);

                Document jobListPage = Jsoup.connect(jobsListURL + i).get();
                Elements jobVacanciesTags = jobListPage.getElementById("listagemVagas").getElementsByTag("li");

                for (Element jobVacancyTag : jobVacanciesTags) {

                    if (jobVacancyTag.attr("id").equals("") || jobVacancyTag.attr("id") == null) {
                        System.out.println("Doesn't have 'id' attribute. It will be ignored.");
                        continue;
                    }

                    String jobPageURL = jobVacancyTag.getElementsByTag("a").get(0).attr("href");
                    Document jobPage = Jsoup.connect(jobPageURL).get();
                    String jobTitle = jobPage.getElementById("anchorTituloVaga").text();

                    System.out.println("    #" + (currentJob++) + " " + jobTitle + " (" + jobPage.location() + ")");

                    Element jobDescriptionTag = jobPage.getElementById("descricao-da-vaga").getElementsByTag("p").get(0);
                    String jobDescription = jobDescriptionTag.text().toLowerCase();

                    for (String termsLine : searchTerms.keySet()) {

                        String regexFindTerm = Util.getFindTermRegex(termsLine);

                        if (jobDescription.matches(regexFindTerm) || jobTitle.matches(regexFindTerm)) {
                            searchTerms.put(termsLine, searchTerms.get(termsLine) + 1);
                            System.out.println("        Found " + termsLine);
                        }
                    }

                    jobCounter++;
                }
            }
            catch (Exception ex){
                ex.printStackTrace();
                exceptions++;
            }
        }

        System.out.println("\n========================================");
        System.out.println("Saving collected data to a csv file.");
        System.out.println("========================================\n");

        LinkedHashMap<String, Integer> termsFrequencySorted = Util.sortHashMapByValues(searchTerms);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, true))) {
            for (String searchTerm : termsFrequencySorted.keySet()) {
                writer.write(searchTerm + ";" + searchTerms.get(searchTerm) + ";" + String.format("%.2f", searchTerms.get(searchTerm) * 100.0 / jobCounter) + "%");
                writer.newLine();
            }
            String todayDate = new SimpleDateFormat("dd/MM/yy").format(new Date());
            writer.write("\nTotal:;" + jobCounter);
            writer.write("\nDate:;" + todayDate);
            writer.write("\nSearch Keyword:;" + searchKeyword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("=====================================");
        System.out.println("Finished with " + exceptions + " exceptions.");
        System.out.println("=====================================\n");
    }

}
