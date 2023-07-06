package scrapers.ditich.nguoikesu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ditich.DiTich;



public class DiTichScraper {
	
	private static ArrayList<DiTich> diTichs = new ArrayList<>();
	private static Document doc;
	private static BufferedReader reader;
	private static int count = 0;
	private static HashSet<String> suKienUrl = new HashSet<>();
	private static HashSet<String> nhanVatUrl = new HashSet<>();
	
	
	public static void scrape() {
		DiTich diTich = new DiTich();
		
		// Ten
		String ten = doc.select("div.page-header>h2[itemprop=\"headline\"]").text();
		diTich.setTen(ten);
		
		// Dia Chi
		String diaChi = doc.select("div.infobox").select("th:containsOwn(Vị trí) + td, th:containsOwn(Địa chỉ) + td, th:containsOwn(Địa điểm) + td, th:containsOwn(Khu vực) + td").html().replaceAll("(?s)<sup.*?</sup>", "");
		if(diaChi.length() != 0) diTich.setDiaChi(Jsoup.parse(diaChi).text());
		
		// Mieu Ta
		Elements p = doc.select("div[itemprop=\"articleBody\"]  p:has(b)");
		if(!p.isEmpty()) {
			String mieuTa = doc.selectFirst("div[itemprop=\"articleBody\"]  p:has(b)").html().replaceAll("(?s)<sup.*?</sup>", "");
			String str = Jsoup.parse(mieuTa).text();	
			if(mieuTa.length() != 0) diTich.setMieuTa(str);
		}

		// Anh
		String anh = doc.selectFirst("tr img[src~=(?i)\\.(png|jpe?g|gif)], div.thumbnail img[src~=(?i)\\.(png|jpe?g|gif)]").attr("src");
		String image = "https://nguoikesu.com" + anh;
		if(anh.length() != 0) diTich.setAnh(image);
		
		// Nguon
		diTich.setNguon(3);
		
		// Nhan Vat & Su Kien Lien Quan
		ArrayList<String> nhanVatLienQuan = new ArrayList<>();
		ArrayList<String> suKienLienQuan = new ArrayList<>();
		Elements ele = doc.select("div[itemprop=\"articleBody\"] a");
		for(Element e:ele) {
			String str = "https://nguoikesu.com" + e.attr("href");
			if(suKienUrl.contains(str)) {
				if(!suKienLienQuan.contains(str)) suKienLienQuan.add(str);
			}
			if(nhanVatUrl.contains(str)) {
				if(!nhanVatLienQuan.contains(str)) nhanVatLienQuan.add(str);
			}
		}
		if(!suKienLienQuan.isEmpty()) diTich.setSuKienLienQuan(suKienLienQuan);	
		if(!nhanVatLienQuan.isEmpty()) diTich.setNhanVatLienQuan(nhanVatLienQuan);	
		
		diTichs.add(diTich);
		count++;
	}
	

	public static void main(String[] args) {
		
		try(FileReader reader = new FileReader("file\\figure-source-3.json")){
			JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
			for(JsonElement jsonElement:jsonArray) {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				String url = jsonObject.get("url").getAsString();
				nhanVatUrl.add(url);
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		try(FileReader reader = new FileReader("file\\event-source-3.json")){
			JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
			for(JsonElement jsonElement:jsonArray) {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				String url = jsonObject.get("url").getAsString();
				suKienUrl.add(url);
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		try {
			reader = new BufferedReader(new FileReader("file\\site-source-3.txt"));
			String line = reader.readLine();
			while (line != null) {
				try {
					doc = Jsoup.connect(line).get();
					System.out.println(line);
				} catch (IOException e) {
					System.out.println("Không thể kết nối tới trang web "+line);
				}
				scrape();
				
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
//		try {
//			doc = Jsoup.connect("https://nguoikesu.com/dia-danh/thanh-nha-ho").get();
//		} catch (IOException e) {
//			System.out.println("Không thể kết nối tới trang web.");
//			return;
//		}
//		scrape();	
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json;
		json = gson.toJson(diTichs);
		String outputFile = "file\\site-source-3.json";
		try (FileWriter writer = new FileWriter(outputFile)) {
			writer.write(json);
			System.out.println("Dữ liệu đã được ghi vào file " + outputFile);
			System.out.println(count + " bản ghi.");
		} catch (IOException e) {
			System.out.println("Ghi dữ liệu vào file thất bại.");
		}	
	}

}