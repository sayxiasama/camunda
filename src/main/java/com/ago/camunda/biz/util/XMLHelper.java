package com.ago.camunda.biz.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;

public class XMLHelper {

    public static String ObjectToXml(Object object) throws Exception {
        JAXBContext context = JAXBContext.newInstance(object.getClass());    // 获取上下文对象
        Marshaller marshaller = context.createMarshaller(); // 根据上下文获取marshaller对象
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "GB2312");  // 设置编码字符集
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // 格式化XML输出，有分行和缩进
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(object, baos);
        String xmlObj = baos.toString();         // 生成XML字符串
        return xmlObj.trim();
    }
}
