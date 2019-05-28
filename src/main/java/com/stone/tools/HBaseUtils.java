package com.stone.tools;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * @author stone
 * @date 2019/5/27 11:45
 * description
 */
public class HBaseUtils {
    private static Configuration conf = null;
    private static Connection conn = null;
    private static Admin admin = null;

    static {
        //HBaseConfiguration conf = new HBaseConfiguration();
        conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "172.30.60.62");
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        //HBaseAdmin admin = new HBaseAdmin(conf);
        //boolean tableExists = admin.tableExists(tableName);

        try {
            conn = ConnectionFactory.createConnection(conf);
            admin = conn.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void close(Connection conn, Admin admin) {
        if (conn != null) {
            try {
                conn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (admin != null) {
            try {
                admin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 判断表是否存在
    public static boolean tableExists(String tableName) throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
        boolean tableExists = admin.tableExists(TableName.valueOf(tableName));
        return tableExists;
    }

    //创建表
    public static void createTable(String tableName, String... cfs) throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
        if (tableExists(tableName)) {
            System.out.println("表已存在！！");
            return;
        }
        //创建表描述器
        HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
        //添加列族
        for (String cf : cfs) {
            HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(cf);
            hTableDescriptor.addFamily(hColumnDescriptor);
        }
        admin.createTable(hTableDescriptor);
        System.out.println("表创建成功！！");
    }

    //删除表
    public static void deleteTable(String tableName) throws IOException {
        if (!tableExists(tableName)) {
            return;
        }
        //使表不可用
        admin.disableTable(TableName.valueOf(tableName));
        //执行删除操作
        admin.deleteTable(TableName.valueOf(tableName));
    }

    //增&改
    public static void putData(String tableName, String rowKey, String cf, String cn, String value) throws IOException {
        if (!tableExists(tableName)) {
            return;
        }
        //HTable table = new HTable(conf, TableName.valueOf(tableName));
        //获取表对象
        Table table = conn.getTable(TableName.valueOf(tableName));
        //创建put对象
        Put put = new Put(Bytes.toBytes(rowKey));
        //添加数据
        put.addColumn(Bytes.toBytes(cf), Bytes.toBytes(cn), Bytes.toBytes(value));
        //执行添加操作
        table.put(put);
        table.close();
    }

    // 追加插入(将原有value的后面追加新的value，如原有value=a追加value=bc则最后的value=abc)
    public static void appendData(String tableName, String rowKey, String columnFamily, String column, String value) throws IOException {
        if (!tableExists(tableName)) {
            return;
        }
        //HTable table = new HTable(conf, TableName.valueOf(tableName));
        //获取表对象
        Table table = conn.getTable(TableName.valueOf(tableName));
        // 通过rowkey创建一个append对象
        Append append = new Append(Bytes.toBytes(rowKey));
        // 在append对象中设置列族、列、值
        append.add(Bytes.toBytes(columnFamily), Bytes.toBytes(column), Bytes.toBytes(value));
        // 追加数据
        table.append(append);
        // 关闭资源
        table.close();
    }

    // 计数器(amount为正数则计数器加，为负数则计数器减，为0则获取当前计数器的值)
    public static Long incrementColumnValue(String tableName, String rowKey, String columnFamily, String column, long amount) throws IOException {
        if (!tableExists(tableName)) {
            return 0L;
        }
        //HTable table = new HTable(conf, TableName.valueOf(tableName));
        //获取表对象
        Table table = conn.getTable(TableName.valueOf(tableName));
        // 计数器
        long result = table.incrementColumnValue(Bytes.toBytes(rowKey), Bytes.toBytes(columnFamily), Bytes.toBytes(column), amount);
        // 关闭资源
        table.close();
        return result;
    }

    //删除
    public static void deleteData(String tableName, String rowKey, String cf, String cn) throws IOException {
        if (!tableExists(tableName)) {
            return;
        }
        Table table = conn.getTable(TableName.valueOf(tableName));
        Delete delete = new Delete(Bytes.toBytes(rowKey));
        delete.addColumns(Bytes.toBytes(cf), Bytes.toBytes(cn));
        //delete.addColumn(Bytes.toBytes(cf), Bytes.toBytes(cn)); 慎用，只会删除最新版本的数据
        table.delete(delete);
        table.close();
    }

    //查询
    public static void scanTable(String tableName) throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
        if (!tableExists(tableName)) {
            return;
        }
        Table table = conn.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        ResultScanner results = table.getScanner(scan);
        for (Result result : results) {
            Cell[] cells = result.rawCells();
            for (Cell cell : cells) {
                System.out.println("RK:" + Bytes.toString(CellUtil.cloneRow(cell))
                        + ",CF:" + Bytes.toString(CellUtil.cloneFamily(cell))
                        + ",CN:" + Bytes.toString(CellUtil.cloneQualifier(cell))
                        + ",VALUE:" + Bytes.toString(CellUtil.cloneValue(cell)));
            }
        }
        table.close();
    }

    public static String getData(String tableName, String rowKey, String cf, String cn) throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
        if (!tableExists(tableName)) {
            return null;
        }
        Table table = conn.getTable(TableName.valueOf(tableName));
        Get get = new Get(Bytes.toBytes(rowKey));
        byte[] value = table.get(get).getValue(Bytes.toBytes(cf), Bytes.toBytes(cn));
        if (value == null || value.length == 0) {
            table.close();
            return null;
        } else {
            table.close();
            return Bytes.toString(value);
        }

//        get.addColumn(Bytes.toBytes(cf), Bytes.toBytes(cn));
//		get.setMaxVersions();
//        Result result = table.get(get);
//        Cell[] cells = result.rawCells();
//
//        for (Cell cell : cells) {
//            System.out.println("RK:" + Bytes.toString(CellUtil.cloneRow(cell))
//                    + ",CF:" + Bytes.toString(CellUtil.cloneFamily(cell))
//                    + ",CN:" + Bytes.toString(CellUtil.cloneQualifier(cell))
//                    + ",VALUE:" + Bytes.toString(CellUtil.cloneValue(cell)));
//        }
//        table.close();
    }

    public static void main(String[] args) throws Exception {

//		System.out.println(tableExists("staff"));
//		createTable("staff", "info");
//		System.out.println(tableExists("staff"));
//		deleteTable("staff");
//		System.out.println(tableExists("staff"));

//		putData("student", "1003", "info", "name", "tom");
//		deleteData("student", "1001", "info", "age");
//		scanTable("student");
//      getData("student", "1002", "info", "name");

        putData("iqiyi.log", "1001", "info", "name", "tom");

        close(conn, admin);
    }
}
