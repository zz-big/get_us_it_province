import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


/**
 * Description:
 *
 * @author zz
 * @date 2022/3/7
 */
public class MapBaiduAddressUDF extends UDF{
    public Text evaluate (String lat, String lng){
        String s="";
        if (null==lat || null==lng){
            return null;
        }
        try {
            s=getLocationByBaiduMap(lng, lat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Text(s);
    }

    public static void main(String[] args) {
        String lat="31.931";
        String lng="120.961";
        Text evaluate = new MapBaiduAddressUDF().evaluate(lat,lng);

        System.out.println(evaluate.toString());
    }


    public static String getLocationByBaiduMap(String longitude,String latitude) throws Exception {
        String ak = "YWdGplhYjUGQ3GtpKNeuTM2S";

        String locJson = geturl("http://api.map.baidu.com/geoconv/v1/?coords=" + longitude + "," +latitude + "&from=1&to=5&ak=" + ak);
        System.out.println(locJson);

        JSONObject jobject =  JSON.parseObject(locJson);
        JSONArray jsonArray = jobject.getJSONArray("result");
        String lat=jsonArray.getJSONObject(0).getString("y");
        String lng=jsonArray.getJSONObject(0).getString("x");
        //System.out.println(lat);

        String addrJson = geturl("http://api.map.baidu.com/reverse_geocoding/v3/?ak="+ ak +"&location=" + lat + "," + lng + "&output=json&pois=1");
        System.out.println(addrJson);

        JSONObject jobjectaddr =  JSON.parseObject(addrJson);
        JSONObject rJsonObject = jobjectaddr.getJSONObject("result");
        System.out.println(rJsonObject.getJSONObject("addressComponent").getString("city"));
        System.out.println(rJsonObject.getJSONObject("addressComponent").getString("province"));

        String addr=jobjectaddr.getJSONObject("result").getString("formatted_address");
        return addr;
    }

    private static String geturl(String geturl) throws Exception {
        //?????????webservice???url
        URL url = new URL(geturl);
        //??????http??????,??????connection??????
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        //???????????????????????????
        httpURLConnection.setRequestMethod("POST");
        //???????????????????????????
        httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        //??????????????????
        httpURLConnection.setDoOutput(true);
        //??????????????????
        httpURLConnection.setDoInput(true);
        //????????????,???????????????
        OutputStream outputStream = httpURLConnection.getOutputStream();
        //?????????soap???????????????
        String content = "user_id="+ URLEncoder.encode("??????Id", "utf-8");
        //????????????
        outputStream.write(content.getBytes());
        //????????????
        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null){
            buffer.append(line);
        }
        String str = buffer.toString();
        return str;
    }

}
