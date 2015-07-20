package importCity;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class Excecuter {
    private static SqlHelper sqlHelper = null;
    public static void main(String[]args)throws Exception{
        sqlHelper = new SqlHelper("jdbc:mysql://121.43.104.22:3306/onlineretailers?characterEncoding=UTF-8","root","4289912wang");
        File file = new File("Provinces.xml");
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(file);
        Document cityDocument = saxBuilder.build(new File("Cities.xml"));
        Document districtDocument = saxBuilder.build(new File("Districts.xml"));
        Element root = document.getRootElement();
        Element cityRoot = cityDocument.getRootElement();
        Element districtRoot = districtDocument.getRootElement();
        List<Element> elements = root.getChildren();
        List<Element> cityElements = cityRoot.getChildren();
        List<Element> districtElements = districtRoot.getChildren();
        for(Element element:elements){
            List<City> cities = new ArrayList<City>();
            String elementId = element.getAttribute("ID").getValue();
            City city = new City();
            String cityId = UUID.randomUUID().toString();
            city.setId(cityId);
            city.setName(element.getAttributeValue("ProvinceName"));
            city.setType("1");
            city.setParentId("0");
            cities.add(city);
            for(Element cityElement:cityElements){
                String cityPid = cityElement.getAttributeValue("PID");
                String city2Id = cityElement.getAttributeValue("ID");
                System.out.println(cityPid);
                if(cityPid.equals(elementId)){
                    City city2 = new City();
                    String cityId2 = UUID.randomUUID().toString();
                    city2.setId(cityId2);
                    city2.setName(cityElement.getAttributeValue("CityName"));
                    city2.setType("2");
                    city2.setParentId(cityId);
                    cities.add(city2);
                    
                    for(Element districtElement:districtElements){
                        if(city2Id.equals(districtElement.getAttributeValue("CID"))){
                            City city3 = new City();
                            String cityId3 = UUID.randomUUID().toString();
                            city3.setId(cityId3);
                            city3.setParentId(cityId2);
                            city3.setName(districtElement.getAttributeValue("DistrictName"));
                            city3.setType("3");
                            cities.add(city3);
                        }
                    }
                }
            }
            pushData(cities);
        }
    }
    
    
    public static void pushData(List<?> data)throws Exception{
        
        sqlHelper.beginTransaction();
        try {
            List<String> sqls = new ArrayList<String>();
            for(Object record:data){
                Class<?> clazz = record.getClass();
                Field[] fields = clazz.getDeclaredFields();
                StringBuffer sql = new StringBuffer();
                sql.append("insert into ");
                Table table = clazz.getAnnotation(Table.class);
                if(table.name()!=null&&!table.name().trim().equals("")){
                    sql.append(table.name());
                }else{
                    sql.append(clazz.getSimpleName());
                }
                sql.append(" ");
                StringBuffer keySql = new StringBuffer();
                keySql.append("(");
                StringBuffer valueSql = new StringBuffer();
                valueSql.append("values(");
                List<Object> values = new ArrayList<Object>();
                for(Field field:fields){
                    Column column = field.getAnnotation(Column.class);
                    if(column!=null){
                        field.setAccessible(true);
                        String name = column.name();
                        if(name==null||name.trim().equals("")){
                            name = field.getName();
                        }
                        Object value = field.get(record);
                        
                        if(value!=null){
                           
                            keySql.append(name);
                
                            keySql.append(",");
                            valueSql.append("'");
                            valueSql.append(StringEscapeUtils.escapeSql(value.toString()));
                            valueSql.append("'");
                            valueSql.append(",");
                            values.add(value);
                        }
                    } 
                }
                keySql.deleteCharAt(keySql.lastIndexOf(","));
                valueSql.deleteCharAt(valueSql.lastIndexOf(","));
                keySql.append(")");
                valueSql.append(")");
                sql.append(keySql);
                sql.append(valueSql);
                System.out.println(sql);
                sqls.add(sql.toString());
            }
            sqlHelper.batchUpdate(sqls.toArray());
        } catch (Exception e) {
           sqlHelper.rollback();
           e.printStackTrace();
        }
        
        sqlHelper.commit();
    }
}
