package com.geeknewbee.doraemon.utils;

import android.content.Context;
import android.util.Xml;

import com.geeknewbee.doraemon.database.Weather_City;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ACER on 2016/8/11.
 */
public class PaserUtil {

    public static List<Weather_City> paserXml(Context context) {
        List<Weather_City> list = null;
        Weather_City city = null;
        try {
            //1.使用Xml获取一个XmlPullParser对象
            XmlPullParser xpp = Xml.newPullParser();
            //2.告诉XmlPullParser对象要解析的是哪个xml文件
            InputStream inputStream = context.getClass().getClassLoader().getResourceAsStream("assets/" + "citys-0226.xml");
            // FileInputStream openFileInput = context.openFileInput("citys-0226.xml");
            xpp.setInput(inputStream, "utf-8");
            //3.获取xml开始行的事件类型。
            int type = xpp.getEventType();
            //4.循环判断事件类型是否是文档结束标记，如果不是，获取下一行的事件类型，继续解析
            while (type != XmlPullParser.END_DOCUMENT) {
                String currentTagName = xpp.getName();
                switch (type) {
                    case XmlPullParser.START_TAG:// 是一个开始标签
                        if (currentTagName.equalsIgnoreCase("citys")) {
                            list = new ArrayList<>();
                        } else if (currentTagName.equalsIgnoreCase("city")) {
                            city = new Weather_City();
                            city.setCityId(xpp.getAttributeValue("", "id"));
                            city.setName(xpp.getAttributeValue("", "name"));
                            city.setNameEn(xpp.getAttributeValue("", "name_en"));
                            city.setNamePy(xpp.getAttributeValue("", "name_py"));
                            city.setProvince(xpp.getAttributeValue("", "province"));
                            city.setWeatherCnId(xpp.getAttributeValue("", "weathercnid"));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (currentTagName.equalsIgnoreCase("city")) {
                            list.add(city);
                        }
                        break;
                }
                // 5.获取下一行时间类型，继续解析
                type = xpp.next();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
