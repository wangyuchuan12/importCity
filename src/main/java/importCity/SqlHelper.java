package importCity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class SqlHelper {
    private Connection connection;
    public SqlHelper(String url , String userName , String password)throws Exception{
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(url, userName, password);
    }
    public void beginTransaction()throws Exception{
        connection.setAutoCommit(false);
    }
    
    public void close()throws Exception{
        connection.close();
    }
    
    public void commit()throws Exception{
        connection.commit();
    }
    public void rollback()throws Exception{
        connection.rollback();
    }
    public void executeUpdate(String sql , Object[] args)throws Exception{
        StringBuffer sb = new StringBuffer();
        sb.append(sql);
        sb.append("[");
        if(args!=null){
            for(Object obj:args){
                sb.append(obj);
                sb.append(",");
            }
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("]");
    //    System.out.println(sb.toString());
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if(args!=null){
            for(int i = 0;i<args.length;i++){
                preparedStatement.setObject(i+1, args[i]);
            }
        }
        preparedStatement.executeUpdate();
    }
    public void batchUpdate(Object[]sqls)throws Exception{
        try {
            Statement statement = connection.createStatement();
            for(Object sql:sqls){
                 statement.addBatch(sql.toString());
            }
            statement.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
            StringBuffer sb = new StringBuffer();
            sb.append("the batch err sqls:[");
            for(Object sql:sqls){
                sb.append(sql+",");
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("]");
            connection.rollback();
            for(Object sql:sqls){
                try {
                    beginTransaction();
                    executeUpdate(sql.toString(),null);
                    commit();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                
            }
        }
        
    }
    public Map<String, Object> getUniqueRecord(String sql , Object[] args)throws Exception{
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if(args!=null){
            for(int i = 0;i<args.length;i++){
                preparedStatement.setObject(i+1, args[i]);
            }
        }
        ResultSet resultSet = preparedStatement.executeQuery();
        java.sql.ResultSetMetaData metaData = resultSet.getMetaData();
        if(resultSet.next()){
            Map<String, Object> map = new HashMap<String, Object>();
            for(int i = 0;i<metaData.getColumnCount();i++){
                String name = metaData.getColumnName(i+1);
                Object value = resultSet.getObject(name);
                map.put(name, value);
            }
            return map;
        }
        return null;
    }
    public Object getUniqueValue(String sql , Object[] args)throws Exception{
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if(args!=null){
        
            for(int i = 0;i<args.length;i++){
                preparedStatement.setObject(i+1, args[i]);
            }
        }
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            return resultSet.getObject(1);
        }
        return null;
    }
    public List<Map<String, Object>> executeQuery(String sql , Object[] args)throws Exception{
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if(args!=null){
            for(int i = 0;i<args.length;i++){
                preparedStatement.setObject(i+1, args[i]);
            }
        }
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Map<String, Object>> resultSetList = new ArrayList<Map<String,Object>>();
        java.sql.ResultSetMetaData metaData = resultSet.getMetaData();
        
        while(resultSet.next()){
            Map<String, Object> map = new HashMap<String, Object>();
            for(int i = 0;i<metaData.getColumnCount();i++){
                String name = metaData.getColumnName(i+1);
                Object value = resultSet.getObject(name);
                map.put(name, value);
            }
            resultSetList.add(map);
            
        }
        return resultSetList;
    }
    public ResultSet executeNativeQuery(String sql , Object[] args)throws Exception{
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if(args!=null){
            for(int i = 0;i<args.length;i++){
                preparedStatement.setObject(i+1, args[i]);
            }
        }
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet;
    }
}
