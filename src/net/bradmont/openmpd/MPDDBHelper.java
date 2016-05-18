package net.bradmont.openmpd;

import net.bradmont.openmpd.models.*;

import net.bradmont.supergreen.*;
import net.bradmont.supergreen.models.*;

import android.content.Context;
import android.database.sqlite.*;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;



public class MPDDBHelper extends DBHelper{
    private static MPDDBHelper instance = null;
    private static int DATABASE_VERSION = 22;

    @Override
    protected void registerModels(){
        instance = this;
        registerModel(new Contact());
        registerModel(new Address());
        registerModel(new EmailAddress());
        registerModel(new PhoneNumber());
        registerModel(new Gift());
        registerModel(new ContactStatus());
        registerModel(new TntService());
        registerModel(new ServiceAccount());
        registerModel(new Notification());
        registerModel(new LogItem());
        registerModel(new QuickMessage());
    }

    public MPDDBHelper(Context c) {
        super(c, DATABASE_VERSION);
        if (instance == null){
            instance = this;
        }
    }
   
    // get the instance without instantiating
    public static MPDDBHelper rawGet(){
        return instance;
    }

    public static MPDDBHelper get(){
        // Thread-safety is a pain; if we aren't instantiated, we'll use
        // the main app activity as context. This means outside threads
        // will have to be careful to take care of this themselves.
        if (instance == null){
            instance = new MPDDBHelper(OpenMPD.get());
        }
        return instance;
    }
    public static ModelList filter(String table_name, String field_name, int value){
        return get()
            .getReferenceModel(table_name)
            .filter(field_name, value);
    }
    
    public static ModelList filter(String table_name, String field_name, float value){
        return get()
            .getReferenceModel(table_name)
            .filter(field_name, value);
    }
    public static ModelList filter(String table_name, String field_name, String value){
        return get()
            .getReferenceModel(table_name)
            .filter(field_name, value);
    }

    public static DBModel getModelByField(String table_name, String field_name, String value){
        return getReferenceModel(table_name).getByField(field_name, value);
    }
    public static DBModel getModelByField(String table_name, String field_name, int value){
        return getReferenceModel(table_name).getByField(field_name, value);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        super.onCreate(db);
        String file = "";
        try {
            file = context.getResources().getResourceName(R.raw.views);
            executeSqlScript(context, db, R.raw.views);
        } catch (IOException e){
            // TODO we should die gracefully here...
            Log.i("net.bradmont.openmpd", "FAILURE: could not open " + file );
        }
    }

    /** 
     * The following methods are taken from Markus Junginger's GreenDAO
     * (https://github.com/greenrobot/greenDAO/), licensed under the APL 2.0
    */

    /**
    * Calls {@link #executeSqlScript(Context, SQLiteDatabase, String, boolean)} with transactional set to true.
    *
    * @return number of statements executed.
    */
    public static int executeSqlScript(Context context, SQLiteDatabase db, int file_id ) throws IOException {
        return executeSqlScript(context, db, file_id, true);
    }

    /**
    * Executes the given SQL asset in the given database (SQL file should be UTF-8). The database file may contain
    * multiple SQL statements. Statements are split using a simple regular expression (something like
    * "semicolon before a line break"), not by analyzing the SQL syntax. This will work for many SQL files, but check
    * yours.
    *
    * @return number of statements executed.
    */
    public static int executeSqlScript(Context context, SQLiteDatabase db, int file_id, boolean transactional)
            throws IOException {
        byte[] bytes = readAsset(context, file_id);
        String sql = new String(bytes, "UTF-8");
        String[] lines = sql.split(";(\\s)*[\n\r]");
        int count;
        if (transactional) {
            count = executeSqlStatementsInTx(db, lines);
        } else {
            count = executeSqlStatements(db, lines);
        }
        return count;
    }

    public static int executeSqlStatementsInTx(SQLiteDatabase db, String[] statements) {
        db.beginTransaction();
        try {
            int count = executeSqlStatements(db, statements);
            db.setTransactionSuccessful();
            return count;
        } finally {
            db.endTransaction();
        }
    }

    public static int executeSqlStatements(SQLiteDatabase db, String[] statements) {
        int count = 0;
        for (String line : statements) {
            line = line.trim();
            if (line.length() > 0) {
                db.execSQL(line);
                count++;
            }
        }
        return count;
    }
    public static int copyAllBytes(InputStream in, OutputStream out) throws IOException {
        int byteCount = 0;
        byte[] buffer = new byte[4096];
        while (true) {
            int read = in.read(buffer);
            if (read == -1) {
                break;
            }
            out.write(buffer, 0, read);
            byteCount += read;
        }
        return byteCount;
    }

    public static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyAllBytes(in, out);
        return out.toByteArray();
    }
    public static byte[] readAsset(Context context, int file_id) throws IOException {
        InputStream in = context.getResources().openRawResource(file_id);
        try {
            return readAllBytes(in);
        } finally {
            in.close();
        }
    }
    @Override
    public synchronized void close(){
        super.close();
        instance = null;
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        super.onUpgrade(db, oldVersion, newVersion);
        if (oldVersion < 10){
            db.execSQL("drop view monthly_base_giving;");
            db.execSQL( "create view monthly_base_giving as select month, sum(base_total) base_giving from (select * from _monthly_base_giving union select * from _monthly_base_giving_in_special_month) group by month;");

        }
        if (oldVersion < 14){
            db.execSQL("update contact_status set notes=NULL;");
        }
        if (oldVersion < 17){
            db.execSQL("update contact_status set partner_type=partner_type*10;");
            db.execSQL("drop table if exists giving_summary_cache;");
            String file = "";
            try {
                file = context.getResources().getResourceName(R.raw.views);
                executeSqlScript(context, db, R.raw.views);
            } catch (IOException e){
                // TODO we should die gracefully here...
                Log.i("net.bradmont.openmpd", "FAILURE: could not open " + file );
            }
        }
        if (oldVersion < 18){
            db.execSQL("drop view _monthly_base_giving;");
            db.execSQL( "create view if not exists _monthly_base_giving as select month, sum(total_gifts) as base_total from partner_giving_status join partner_giving_by_month on partner_giving_status.tnt_people_id = partner_giving_by_month.tnt_people_id where total_gifts<=giving_amount and partner_type=60 group by month order by month; ");
            db.execSQL("drop table if exists giving_summary_cache;");
        }
        if (oldVersion < 19){
            db.execSQL("alter table tnt_service add query_ini_url string;");
            db.execSQL("update tnt_service set query_ini_url='http://tntmpd.powertochange.org/tntmpd/Query.ini', name = 'Power to Change - Canada' where name_short='P2C'");
            db.execSQL("update tnt_service set query_ini_url='https://staffweb.cru.org/ss/tntmpd/query.ini', name = 'Cru - USA' where name_short='CCC'");
            db.execSQL("update tnt_service set query_ini_url='https://rews.cco.ca/dataserver/CCO/dataquery/TntQuery.aspx', name = 'CCO Canada' where name_short='CCO'");
        }
        if (oldVersion < 20){
            db.execSQL("alter table tnt_service add untested_service int default 0;");
        }
        if (oldVersion < 21){
            db.execSQL("alter table log add timestamp string;");
        }
        if (oldVersion < 22){
            db.execSQL("create table contact_sublist (contact_id int, list_name varchar(255));");
        }
    }

}

