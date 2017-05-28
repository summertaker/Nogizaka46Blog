package com.summertaker.nogizaka46blog.parser;

import com.summertaker.nogizaka46blog.common.BaseParser;
import com.summertaker.nogizaka46blog.data.Article;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class Nogizaka46Parser extends BaseParser {

    public void parseBlogList(String response, ArrayList<Article> articles) {
        /*
        <div class="right2in" id="sheet">
            <div class="paginate">
                &nbsp;1&nbsp; |
                <a href="?p=2">&nbsp;2&nbsp;</a> |
                <a href="?p=3">&nbsp;3&nbsp;</a> |
                ...
                <a href="?p=15">&nbsp;15&nbsp;</a> |
                <a href="?p=2">&#65310;</a>
            </div>
            <h1 class="clearfix">
                <span class="date">
                    <span class="yearmonth">2016/03</span>
                    <span class="daydate">
                        <span class="dd1">29</span>
                        <span class="dd2">Tue</span>
                    </span>
                </span>
                <span class="heading">
                    <span class="author">若月佑美</span>
                    <span class="entrytitle">
                        <a href="http://blog.nogizaka46.com/yumi.wakatsuki/2016/03/031299.php" rel="bookmark">1週間も経ってるー(´,,•﹏• ,,｀)</a>
                    </span>
                </span>
            </h1>
            <div class="fkd"></div>
            <div class="entrybody">
                <div>髪のカラーの抜け方が早い。</div>
                <div>
                    <a href="http://dcimg.awalker.jp/img1.php?id=phu2qg0Vf9cBKA4BKCZQ81j2pg07yhJsTMg9A100AdAgpABNlZWV5e9V3A6FWvSiKCcZB7FvYxNFOLuocFWp8LYkc0XhC2u3t81pNarOGlyqtuPV6FCw1WKtIqwG3fT8BCsMfPZbULPaLQtvcFkbr2TRw01VSoafd3WxO6RpxMR70kahY7KwfuKzhg2jkR1Zx40M7Nx1">
                        <img src="http://img.nogizaka46.com/blog/yumi.wakatsuki/img/2016/03/29/8178961/0000.jpeg">
                    </a>
                </div>
            </div>
            <div class="entrybottom">
                2016/03/29 08:00｜
                <a href="http://blog.nogizaka46.com/yumi.wakatsuki/2016/03/031299.php">個別ページ</a>｜
                <a href="http://blog.nogizaka46.com/yumi.wakatsuki/2016/03/031299.php#comments">コメント(3)</a>
            </div>
            ...
        </div>
        */

        //response = Util.getJapaneseString(response, "SHIFT-JIS");
        //Log.e(TAG, response);

        Document doc = Jsoup.parse(response);
        Element root = doc.getElementById("sheet");

        Elements rows = root.select("h1");
        if (rows == null) {
            return;
        }

        Elements entrybodys = root.select(".entrybody");
        if (entrybodys == null) {
            return;
        }

        Elements entrybottoms = root.select(".entrybottom");
        if (entrybottoms == null) {
            return;
        }

        //Log.e(TAG, "rows.size(): " + rows.size());
        //Log.e(TAG, "entrybodys.size(): " + entrybodys.size());
        //Log.e(TAG, "entrybottom.size(): " + entrybottom.size());

        for (int i = 0; i < rows.size(); i++) {
            String id;
            String title;
            String name;
            String date;
            String content;
            String url;

            Element row = rows.get(i);

            Element el;

            //el = row.select(".yearmonth").first();
            //if (el == null) {
            //    continue;
            //}
            //date = el.text().replace("/", "-");
            //date += "-" + row.select(".dd1").first().text();
            //date += " " + row.select(".dd2").first().text();

            el = row.select(".author").first();
            if (el == null) {
                continue;
            }
            name = el.text();

            el = row.select(".entrytitle").first();
            if (el == null) {
                continue;
            }
            el = el.select("a").first();
            title = el.text();
            url = el.attr("href");

            //if (i >= entrybodys.size()) {
            //    break;
            //}
            //Element entitybody = entrybodys.get(i);
            if (entrybottoms.size() > i) {
                Element entrybottom = entrybottoms.get(i);
                String text = entrybottom.text();
                String[] array = text.split("｜");
                date = array[0].trim();
            } else {
                date = null;
            }

            el = row.nextElementSibling();
            Element body = el.nextElementSibling();
            content = body.text().trim();
            content = content.replace("&nbsp;", "");
            content = content.replace(" ", "").replace("　", "");
            content = content.replaceAll("\\p{Z}", "");

            ArrayList<String> imageUrls = new ArrayList<>();
            ArrayList<String> thumbnails = new ArrayList<>();
            //ArrayList<String> duplicates = new ArrayList<>();
            for (Element img : body.select("img")) {
                //Log.e(TAG, a.html());

                String src = img.attr("src");
                if (src.contains(".gif")) {
                    continue;
                }

                boolean exist = false;
                for (String str : thumbnails) {
                    if (src.equals(str)) {
                        exist = true;
                        //duplicates.add(src);
                        break;
                    }
                }
                if (exist) {
                    continue;
                }

                //Log.e(TAG, src);
                thumbnails.add(src);
                //thumbnailUrl += src + "*";

                Element parent = img.parent();
                if (parent.tagName().equals("a")) {
                    imageUrls.add(parent.attr("href"));
                } else {
                    imageUrls.add(null);
                }

                // 이미지 보호장치 있음 - 그냥 웹 뷰로 이동시킬 것
                // http://dcimg.awalker.jp/img1.php?id=phu2qg0Vf9cBKA4BKCZQ81j2pg07yhJsTMg9A100AdAgpABNlZWV5e9V3A6FWvSiKCcZB7FvYxNFOLuocFWp8LYkc0XhC2u3t81pNarOGlyqtuPV6FCw1WKtIqwG3fT8BCsMfPZbULPaLQtvcFkbr2TRw01VSoafd3WxO6RpxMR70kahY7KwfuKzhg2jkR1Zx40M7Nx1
                // http://dcimg.awalker.jp/img2.php?sec_key=phu2qg0Vf9cBKA4BKCZQ81j2pg07yhJsTMg9A100AdAgpABNlZWV5e9V3A6FWvSiKCcZB7FvYxNFOLuocFWp8LYkc0XhC2u3t81pNarOGlyqtuPV6FCw1WKtIqwG3fT8BCsMfPZbULPaLQtvcFkbr2TRw01VSoafd3WxO6RpxMR70kahY7KwfuKzhg2jkR1Zx40M7Nx1
                //imageUrl = imageUrl.replace("/img1.php?id=", "/img2.php?sec_key=");

                //el = img.parent();
                //if (!el.tagName().equals("a")) { // 큰 사진에는 링크가 걸려있음
                //    continue;
                //}
                //imageUrls.add(el.attr("href"));
                //imageUrl = el.attr("href") + "*";

                //boolean exist = false;
                //for (WebData webData : webDataList) {
                //    if (id.equals(webData.getGroupId())) {
                //        exist = true;
                //        break;
                //    }
                //}
            }

            /*
            for (int j = 0; j < thumbnails.size(); j++) {
                String img = thumbnails.get(j);
                boolean valid = true;
                for (String dup : duplicates) {
                    if (img.equals(dup)) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    thumbnailUrl += img + "*";
                    imageUrl = imageUrls.get(j) + "*";
                }
            }

            if (!thumbnailUrl.isEmpty()) {
                thumbnailUrl = thumbnailUrl + "*";
                thumbnailUrl = thumbnailUrl.replace("**", "");

                imageUrl = imageUrl + "*";
                imageUrl = imageUrl.replace("**", "");
            }
            Log.e(TAG, title + " / " + url + " / " + thumbnailUrl + " / " + imageUrl);
            */

            Article article = new Article();
            article.setTitle(title);
            article.setName(name);
            article.setDate(date);
            article.setContent(content);
            article.setUrl(url);
            article.setThumbnails(thumbnails);
            article.setImageUrls(imageUrls);

            articles.add(article);
        }
    }
}