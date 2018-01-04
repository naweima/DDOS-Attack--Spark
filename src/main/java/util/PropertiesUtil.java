package util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.grizzly.servlet.ver25.String;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtil {
    private static final Log log = LogFactory.getLog(PropertiesUtil.class);
    private static Properties env = new Properties();
    private static String fileName;
    PropertiesUtil(){}    //空的构造方法
   public  PropertiesUtil(String fileName) {
        this.fileName=fileName;
            try {
                InputStream is = PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName);
                env.load(is);
                is.close();
            } catch (Exception e) {
                log.error(e);
            }

    }
    /**
     * 获取某个属性
     */
    public String getProperty(String key){
        return env.getProperty(key);
    }
    /**
     * 获取所有属性，返回一个map,不常用
     * 可以试试props.putAll(t)
     */
    public Map getAllProperty(){
        Map map=new HashMap();
        Enumeration enu = env.propertyNames();
        while (enu.hasMoreElements()) {
            String key = (String) enu.nextElement();
            String value = env.getProperty(key);
            map.put(key, value);
        }
        return map;
    }
    /**
     * 在控制台上打印出所有属性，调试时用。
     */
    public void printProperties(){
        env.list(System.out);
    }

    public static void main(String[] args) {
        PropertiesUtil util=new PropertiesUtil("ddosIp.properties");
        util.printProperties();
    }
}