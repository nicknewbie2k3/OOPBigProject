package scrapers.lehoi.wikipedia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import entities.lehoi.LeHoi;
import scrapers.lehoi.FestivalScraper;
import utilities.EntityUrl;

public class ScraperLeHoi {

	public static void main(String[] args) {

		FestivalScraper scraper = new FestivalScraper() {

			@Override
			public void scrape(Document doc) {
				HashSet<String> nhanVat = EntityUrl.getEntityUrl("file\\figure-source-2.json");
				HashSet<String> diTich = EntityUrl.getEntityUrl("file\\site-source-2.json");
				String url = "";
				Element infobox = doc.selectFirst("table.prettytable.wikitable");
				Elements rows = infobox.select("tr");
				Document supDoc;
				for (int i = 1; i < rows.size(); i++) {

					LeHoi leHoi = new LeHoi();
					Elements td = rows.get(i).select("td");

					String thoiGian = td.get(0).text();
					if (!thoiGian.isEmpty())
						leHoi.setThoiGian(thoiGian);

					String diaDiem = td.get(1).text();
					if (!diaDiem.isEmpty())
						leHoi.setDiaDiem(diaDiem);

					Element name = td.get(2);
					String ten = name.text();
					leHoi.setTen(ten);

					String suKienLienQuan = null;
					ArrayList<String> nhanVatLienQuan = new ArrayList<>();
					ArrayList<String> diTichLienQuan = new ArrayList<>();

					if (!name.selectFirst("a").attr("title").contains("không tồn tại")) {
						url = "https://vi.wikipedia.org" + name.selectFirst("a").attr("href");
						if (url.indexOf("#") != -1)
							url = url.substring(0, url.indexOf("#"));
						try {
							supDoc = Jsoup.connect(url).get();
						} catch (IOException e) {
							System.out.println("Không thể kết nối tới trang web." + url);
							break;
						}

						leHoi.setUrl(url);

						Element p;
						String str;
						if (supDoc.selectFirst("p b") == null) {

							str = supDoc.title().split(" – ")[0];
							p = supDoc.selectFirst("p");
						} else {
							str = supDoc.selectFirst("p b").text();
							p = supDoc.selectFirst("p b").parent();

						}
						if (!diTichLienQuan.contains(str) && diTich.contains(url)) {
							diTichLienQuan.add(str);
						}

						String mieuTa = p.text().replaceAll("\\[[^\\]]*\\]", "");
						if (!mieuTa.contains("có thể là"))
							leHoi.setMieuTa(mieuTa);

						Element ul = p.nextElementSibling();
						if (ul != null && ul.tagName().equals("ul")) {
							Elements lis = ul.select("li");
							for (Element li : lis) {
								Element ref = li.selectFirst("a");
								if (ref != null) {
									str = ref.text();
									url = "https://vi.wikipedia.org" + ref.attr("href");
									if (!diTichLienQuan.contains(str) && diTich.contains(url)) {
										diTichLienQuan.add(str);
									}
								}
							}
						}
						// Đoạn văn đầu tiên
						Elements refs = p.select("a");

						String tmp;
						for (Element ref : refs) {
							str = ref.text();
							tmp = str.toLowerCase();
							if (tmp.contains("trận")) {
								suKienLienQuan = str;
							}
							url = "https://vi.wikipedia.org" + ref.attr("href");
							if (!nhanVatLienQuan.contains(str) && nhanVat.contains(url)) {
								nhanVatLienQuan.add(str);
							}
							if (!diTichLienQuan.contains(str) && diTich.contains(url)) {
								diTichLienQuan.add(str.replaceAll("kinh đô ", ""));
							}

						}

						// Đoạn văn thứ hai
						Element p2 = p.nextElementSibling();

						if (p2 != null) {
							while (p2 != null && !p2.tagName().equals("p"))
								p2 = p2.nextElementSibling();
							if (p2 != null) {
								refs = p2.select("a");
								for (Element ref : refs) {
									str = ref.text();
									url = "https://vi.wikipedia.org" + ref.attr("href");
									if (!nhanVatLienQuan.contains(str) && nhanVat.contains(url)) {
										nhanVatLienQuan.add(str);
									}
									if (!diTichLienQuan.contains(str) && diTich.contains(url)) {
										diTichLienQuan.add(str);
									}
								}
							}
						}
						Element link = supDoc.selectFirst("figure img");
						if (link != null)
							leHoi.setAnh(link.attr("src"));
					}

					String lanDauToChuc = td.get(3).text();
					if (!lanDauToChuc.isEmpty())
						leHoi.setLanDauToChuc(lanDauToChuc);

					String nv = td.get(4).text();
					if (nhanVatLienQuan.isEmpty() && !nv.isEmpty()) {
						String[] tmps = nv.split(", ");

						for (String s : tmps) {
							nhanVatLienQuan.add(s);
						}

					}
					if (suKienLienQuan != null)
						leHoi.setSuKienLienQuan(suKienLienQuan);

					if (!nhanVatLienQuan.isEmpty())
						leHoi.setNhanVatLienQuan(nhanVatLienQuan);

					if (!diTichLienQuan.isEmpty())
						leHoi.setDiTichLienQuan(diTichLienQuan);
					else if (ten.indexOf("chùa") != -1) {
						diTichLienQuan.add(ten.substring(ten.indexOf("chùa")));
						leHoi.setDiTichLienQuan(diTichLienQuan);
					} else if (ten.indexOf("miếu") != -1) {
						diTichLienQuan.add(ten.substring(ten.indexOf("miếu")));
						leHoi.setDiTichLienQuan(diTichLienQuan);
					} else if (ten.indexOf("đền") != -1) {
						diTichLienQuan.add(ten.substring(ten.indexOf("đền")));
						leHoi.setDiTichLienQuan(diTichLienQuan);
					} else if (ten.indexOf("đầm") != -1) {
						diTichLienQuan.add(ten.substring(ten.indexOf("đầm")));
						leHoi.setDiTichLienQuan(diTichLienQuan);
					} else if (ten.indexOf("đình") != -1) {
						diTichLienQuan.add(ten.substring(ten.indexOf("đình")));
						leHoi.setDiTichLienQuan(diTichLienQuan);
					}

					leHoi.setNguon(2);

					addLeHoi(leHoi);

				}

			}

		};
		Document doc = scraper.connectToUrl("https://vi.wikipedia.org/wiki/L%E1%BB%85_h%E1%BB%99i_Vi%E1%BB%87t_Nam");
		if (doc != null) {
			scraper.scrape(doc);
			scraper.getJsonString();
			scraper.saveToFile("file\\festival-source-2.json", true);
		}
	}

}
