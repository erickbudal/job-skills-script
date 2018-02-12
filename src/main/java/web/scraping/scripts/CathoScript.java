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

    private static final int WAIT_TIME_PER_ANNOUNCEMENT = 0;
    private static final int WAIT_TIME_PER_X_ANNOUNCEMENTS = 0;

    //=========================== PARAMS ===========================

    private String inputFilePath = "search_terms.txt";
    private String outputFilePath = "catho_java.csv";
    private String searchKeyword = "desenvolvedor";

    //===============================================================

    public void start() {

        HashMap<String, Integer> searchTerms;
        String jobsListURL = "https://www.catho.com.br/vagas/?q=%s&page=";
        int exceptions = 0;
        int jobCounter = 0;
        int actualJob = 1;
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
                System.out.println("Actual page: " + i);

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

                    System.out.println("    #" + (actualJob++) + " " + jobTitle + " (" + jobPage.location() + ")");

                    Element jobDescriptionTag = jobPage.getElementById("descricao-da-vaga").getElementsByTag("p").get(0);
                    String jobDescription = jobDescriptionTag.text().toLowerCase();

                    for (String term : searchTerms.keySet()) {
                        String regexFindTerm = Util.getFindTermRegex(term);
                        if (jobDescription.matches(regexFindTerm) || jobTitle.matches(regexFindTerm)) {
                            searchTerms.put(term, searchTerms.get(term) + 1);
                            System.out.println("        Found " + term);
                        }
                    }

                    jobCounter++;

                    if(jobCounter % 150 == 0){
                        System.out.println("\nWaiting " + WAIT_TIME_PER_X_ANNOUNCEMENTS + " milliseconds to continue...\n");
                        Thread.sleep(WAIT_TIME_PER_X_ANNOUNCEMENTS);
                    }
                    else{
                        Thread.sleep(WAIT_TIME_PER_ANNOUNCEMENT);
                    }
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("=====================================");
        System.out.println("Finished with " + exceptions + " exceptions.");
        System.out.println("=====================================\n");
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }
}
