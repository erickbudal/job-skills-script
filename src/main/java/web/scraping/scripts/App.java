package web.scraping.scripts;

public class App {

    public static void main(String[] args) {

        String inputFilePath = "search_terms.txt";
        String outputFilePath = "/home/erick/Downloads/empregos_resultado.csv";
        String searchKeyword = "desenvolvedor java";

        new CathoScript(inputFilePath, outputFilePath, searchKeyword).start();
        //new GlassdoorScript(inputFilePath, outputFilePath, searchKeyword, 1).start();
    }
}
