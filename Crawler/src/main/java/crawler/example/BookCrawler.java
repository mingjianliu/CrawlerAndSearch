package crawler.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class BookCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz))$");

    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "https://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches();
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            boolean isPageForBook = htmlParseData.getMetaTags().getOrDefault("og:type", "").equals("book");
            if (isPageForBook) {
                // TODO: add comments/ to fetch reviews
                try {
                    URL url = new URL("http://localhost:8080/book/create/");

                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");

                    con.setRequestProperty("Content-Type", "application/json; utf-8");
                    con.setRequestProperty("Accept", "application/json");

                    con.setDoOutput(true);

                    //JSON String need to be constructed for the specific resource.
                    //We may construct complex JSON using any third-party JSON libraries such as jackson or org.json
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("title", htmlParseData.getTitle());
                    parameters.put("html", htmlParseData.getHtml());
                    for (Map.Entry<String, String> e : htmlParseData.getMetaTags().entrySet()) {
                        parameters.put(e.getKey(), e.getValue());
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonInputString;
                    try {
                        jsonInputString = objectMapper.writeValueAsString(parameters);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return;
                    }

                    try (OutputStream os = con.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int code = con.getResponseCode();
                    System.out.println(code);

//                    int status = con.getResponseCode();
//                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//                    String inputLine;
//                    StringBuilder content = new StringBuilder();
//                    while ((inputLine = in.readLine()) != null) {
//                        content.append(inputLine);
//                    }
//                    in.close();

//                    assertEquals("status code incorrect", status, 200);
//                    assertTrue("content incorrect", content.toString()
//                            .contains("Example Domain"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            System.out.println("Number of outgoing links: " + links.size());
        }
    }

}
