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

public class EmpregosScript {

    private static final int WAIT_TIME_PER_ANNOUNCEMENT = 0;
    private static final int WAIT_TIME_PER_X_ANNOUNCEMENTS = 0;

    //=========================== PARAMS ===========================

    private String inputFilePath = "search_terms.txt";
    private String outputFilePath = "empregos_desenvolvedor.csv";
    private String searchKeyword = "desenvolvedor";

    //===============================================================

    public void start() {

        HashMap<String, Integer> searchTerms;
        String jobsListURL = "https://www.empregos.com.br/vagas/p%d/%s";
        int exceptions = 0;
        int jobCounter = 0;
        int actualJob = 1;
        int totalPages;

        searchKeyword = searchKeyword.replaceAll(" ", "+");

        searchTerms = Util.loadSearchTerms(inputFilePath);

        try {
            Document jobListPage = Jsoup.connect(String.format(jobsListURL, 1, searchKeyword)).get();
            String totalJobsText = jobListPage.select("#ctl00_ContentBody_pPaiResultadoTopo > strong:nth-child(3)").get(0).text();
            totalPages = Integer.parseInt(totalJobsText.replaceAll("\\.", "")) / 10 + 1;
        }
        catch (Exception ex){
            ex.printStackTrace();
            return;
        }

        for (int i = 1; i < totalPages; i++) {
            try {
                System.out.println("Actual page: " + i + "/" + totalPages);

                Document jobListPage = Jsoup.connect(String.format(jobsListURL, i, searchKeyword)).get();
                Elements jobVacanciesTags = jobListPage.getElementsByTag("h3");

                for (Element jobVacancyTag : jobVacanciesTags) {
                    try {
                        String jobPageURL = jobVacancyTag.getElementsByTag("a").get(0).attr("href");
                        Document jobPage = Jsoup.connect(jobPageURL).get();

                        if (jobPage.select("head > title").get(0).text().equals("Tela de erro")) {
                            System.out.println("\n    Found a error screen. This job announcement will be ignored.\n");
                            continue;
                        }

                        System.out.println("    #" + (actualJob++) + " " + jobPage.getElementById("ctl00_ContentBody_h1TituloVaga").text() + " (" + jobPage.location() + ")");

                        Element jobDescriptionTagA = jobPage.getElementById("ctl00_ContentBody_trDescricaoVaga").getElementsByTag("p").get(0);
                        Element jobDescriptionTagB = jobPage.getElementById("ctl00_ContentBody_trQualificacao").getElementsByTag("p").get(0);

                        String jobDescription = "";
                        if (jobDescriptionTagA != null) {
                            jobDescription = jobDescriptionTagA.text().toLowerCase() + " ";
                        }
                        if (jobDescriptionTagB != null) {
                            jobDescription += jobDescriptionTagB.text().toLowerCase();
                        }

                        for (String term : searchTerms.keySet()) {
                            String regexFindTerm = Util.getFindTermRegex(term);
                            if (jobDescription.matches(regexFindTerm)) {
                                searchTerms.put(term, searchTerms.get(term) + 1);
                                System.out.println("        Found " + term);
                            }
                        }

                        jobCounter++;

                        if (jobCounter % 100 == 0) {
                            System.out.println("\nWaiting " + WAIT_TIME_PER_X_ANNOUNCEMENTS + " milliseconds to continue...\n");
                            Thread.sleep(WAIT_TIME_PER_X_ANNOUNCEMENTS);
                        } else {
                            Thread.sleep(WAIT_TIME_PER_ANNOUNCEMENT);
                        }
                        if(jobCounter >= 900){
                            break;
                        }
                    }
                    catch(Exception ex){
                        ex.printStackTrace();
                        exceptions++;
                    }
                }
            }
            catch (Exception ex){
                ex.printStackTrace();
                exceptions++;
            }
        }
        System.out.println("\n--------------------------------------");
        System.out.println("Saving collected data to a csv file.");
        System.out.println("--------------------------------------\n");

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
