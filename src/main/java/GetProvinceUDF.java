import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

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


@Description(name = "get_province_udf",
        value = "_FUNC_(lng,lat) - Returns province name",
        extended = "Example:\n"
                + " > SELECT _FUNC_('10.5050949','43.8450298'); \n")
public class GetProvinceUDF extends GenericUDF {
    private static Logger logger = Logger.getLogger(GetProvinceUDF.class);

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        if (arguments.length != 2) {
            throw new UDFArgumentException("The operator 'get_province_udf' accepts 2 arguments.");
        }
        ObjectInspector returnType = PrimitiveObjectInspectorFactory.getPrimitiveJavaObjectInspector(PrimitiveObjectInspector.PrimitiveCategory.STRING);
        return returnType;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Object lng = arguments[0].get();
        Object lat = arguments[1].get();
        if (null == lat || null == lng) {
            return null;
        }
        try {
            return getLocationByBaiduMap(String.valueOf(lng), String.valueOf(lat));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public String getDisplayString(String[] children) {
        StringBuilder sb = new StringBuilder();
        sb.append("?????? " + children[0] + "," + children[1] + "???????????????")
                .append("\n")
                .append("Usage: get_province_udf(lng,lat)")
                .append("\n")
                .append("?????????");
        return sb.toString();
    }


    public static String getLocationByBaiduMap(String longitude, String latitude) throws Exception {
        String ak = "WZNL9GKwfdtMvC3ZlZ9PuVRTMszezvrc";

        String locJson = geturl("http://api.map.baidu.com/geoconv/v1/?coords=" + longitude + "," + latitude + "&from=1&to=5&ak=" + ak);

        JSONObject jobject = JSON.parseObject(locJson);
        JSONArray jsonArray = jobject.getJSONArray("result");
        String lat = jsonArray.getJSONObject(0).getString("y");
        String lng = jsonArray.getJSONObject(0).getString("x");

        String addrJson = geturl("http://api.map.baidu.com/reverse_geocoding/v3/?ak=" + ak + "&location=" + lat + "," + lng + "&output=json&pois=1");

        JSONObject jobjectaddr = JSON.parseObject(addrJson);

        String province = jobjectaddr.getJSONObject("result").getJSONObject("addressComponent").getString("province");
        return province;
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
        String content = "user_id=" + URLEncoder.encode("??????Id", "utf-8");
        //????????????
        outputStream.write(content.getBytes());
        //????????????
        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        String str = buffer.toString();
        return str;
    }
}
