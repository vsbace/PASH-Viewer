/*
 *-------------------
 * The DBUtils.java is part of PASH Viewer
 *-------------------
 * 
 * Copyright (c) 2018, Dmitry Tsvetkov
 *
 */
package org.ash.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.security.MessageDigest;
import java.math.BigInteger;
import org.ash.util.Options;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;

public class DBUtils {

    public static String GetSQLCommandType(String nsql) {

        String[] array = nsql.split(" ", -1);

        for (int i = 0; i < array.length; i++) {
            if (array[i].equals("select")) {
                return "SELECT";
            } else if (array[i].equals("update")) {
                return "UPDATE";
            } else if (array[i].equals("insert")) {
                return "INSERT";
            } else if (array[i].equals("delete")) {
                return "DELETE";
            } else if (array[i].equals("with")) {
		int s = 0;
                for (int j = i; j < array.length; j++) {
			if (array[j].equals("(")) { s++; }
			else if (array[j].equals(")")) { s--; }
			else if (s == 0) {
		            if (array[j].equals("select")) {
		                return "SELECT";
		            } else if (array[j].equals("update")) {
		                return "UPDATE";
		            } else if (array[j].equals("insert")) {
		                return "INSERT";
		            } else if (array[j].equals("delete")) {
		                return "DELETE";
		            } 
			}
		}
		return "UNKNOWN";
            } else if (array[i].equals("drop")) {
                return "DROP";
            } else if (array[i].equals("truncate")) {
                return "TRUNCATE";
            } else if (array[i].equals("end")) {
                return "COMMIT";
            } else if (array[i].equals("commit")) {
                return "COMMIT";
            } else if (array[i].equals("rollback")) {
                return "ROLLBACK";
            } else if (array[i].equals("begin")) {
                return "BEGIN";
            } else if (array[i].equals("vacuum")) {
                return "VACUUM";
            } else if (array[i].equals("autovacuum:")||array[i].equals("autovacuum")) {
                return "AUTOVACUUM";
            } else if (array[i].equals("analyze")) {
                return "ANALYZE";
            } else if (array[i].equals("explain")) {
                return "EXPLAIN";
            } else if (array[i].equals("execute")) {
                return "EXECUTE";
            } else if (array[i].equals("do")) {
                return "ANONYMOUS BLOCK";
            } else if (array[i].equals("prepare")) {
                return "PREPARE";
            } else if (array[i].equals("perform")) {
                return "PERFORM";
            } else if (array[i].equals("lock")) {
                return "LOCK";
            } else if (array[i].equals("copy")) {
                return "COPY";
            } else if (array[i].equals("backup")) {
                return "BACKUP";
            } else if (array[i].equals("wal")) {
                return "WAL EXCHANGE";
            } else if (array[i].equals("alter")) {
                if (array[i + 1].equals("index")) {
                    return "ALTER INDEX";
                } else if (array[i + 1].equals("table")) {
                    return "ALTER TABLE";
                } else {
                    return "ALTER";
                }
            } else if (array[i].equals("create")) {
                if (array[i + 1].equals("index")) {
                    return "CREATE INDEX";
                } else if (array[i + 1].equals("table")) {
                    return "CREATE TABLE";
                } else if (array[i + 1].equals("database")) {
                    return "CREATE DATABASE";
                } else {
                    return "CREATE";
                }
            }
            return "UNKNOWN (" + array[0] + ")";
        }

        return "";
    }

    public static String NormalizeSQL(String sql) {

        sql = sql.replaceAll("\\n", " ");
        sql = sql.replaceAll("\\(", " ( ");
        sql = sql.replaceAll("\\)", " ) ");
        sql = sql.replaceAll(",", " , ");
        sql = sql.replaceAll("'", " ' ");
        sql = sql.replaceAll("=", " = ");
        sql = sql.replaceAll("<", " < ");
        sql = sql.replaceAll(">", " > ");
        sql = sql.replaceAll(";", "");
        sql = sql.replaceAll("[ ]+", " ");
        sql = sql.replaceAll("> =", ">=");
        sql = sql.replaceAll("< =", "<=");

        sql = sql.toLowerCase().trim();
        String[] array = sql.split(" ", -1);

        int var_number = 0;
        String normalized_sql = "";
        Boolean quote_flag = false;

        for (int i = 0; i < array.length; i++) {
            if (array[i].equals("'")) {
                if (!quote_flag) {
                    quote_flag = true;
                    var_number++;
                    normalized_sql += "$" + var_number + " ";
                } else {
                    quote_flag = false;
                }
            } else if (quote_flag) {
                continue;
            } else if (array[i].matches("-?\\d+(\\.\\d+)?")) {
                var_number++;
                normalized_sql += "$" + var_number + " ";
            } else if (array[i].equals("order")) {
                for (int j = i; j < array.length; j++) {
                    normalized_sql += array[j] + " ";
                }
                return normalized_sql.trim();
            } else {
                normalized_sql += array[i] + " ";
            }
        }

        return normalized_sql.trim();
    }

    public static String PlanHashValue(String plan) {

        plan = plan.replaceAll("\\n", " ");
        plan = plan.replaceAll("\\(", " ( ");
        plan = plan.replaceAll("\\)", " ) ");
        plan = plan.replaceAll(",", " , ");
        plan = plan.replaceAll("'", " ' ");
        plan = plan.replaceAll("=", " = ");
        plan = plan.replaceAll("<", " < ");
        plan = plan.replaceAll(">", " > ");
        plan = plan.replaceAll("[ ]+", " ");
        plan = plan.replaceAll("> =", ">=");
        plan = plan.replaceAll("< =", "<=");

        plan = plan.toLowerCase().trim();
        String[] array = plan.split(" ", -1);

        int var_number = 0;
        String normalized_plan = "";
        Boolean quote_flag = false;

        for (int i = 0; i < array.length; i++) {
            if (array[i].equals("'")) {
                if (!quote_flag) {
                    quote_flag = true;
                    var_number++;
                    normalized_plan += "$" + var_number + " ";
                } else {
                    quote_flag = false;
                }
            } else if (quote_flag) {
                continue;
            } else if (array[i].matches("-?\\d+(\\.\\d+)?")) {
                var_number++;
                normalized_plan += "$" + var_number + " ";
            } else if (array[i].equals("cost") || array[i].equals("rows") || array[i].equals("width")) {
                if (array[i + 1].equals("=")) {
                    var_number++;
                    array[i + 2] = "$" + var_number;
                }
                normalized_plan += array[i] + " ";
            } else {
                normalized_plan += array[i] + " ";
            }
        }

        normalized_plan = normalized_plan.trim();

        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(normalized_plan.getBytes());
            digest = messageDigest.digest();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String plan_hash_value = bigInt.toString(10).substring(0, 10);

        return plan_hash_value;
    }

    public static String md5Custom(String st) {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16).substring(0, 13);

        return md5Hex;
    }

    // запрашивает план из БД и пишет его локально в файл
    public static void explainPlan(String sqlId, String query_text, String command_type, String planDir, String fileSeparator, String connDBName, String databaseName, Connection conn, int explainFreq) {

        String planFileName = planDir + fileSeparator + sqlId + ".plan";
        String textFileName = planDir + fileSeparator + sqlId + ".sql";
        File planFile = new File(planFileName);

        String plan = "";
        String planHashValue = "0000000000";

        // если mtime файла старше explainFreq ms - запрашиваем план заново
        if (System.currentTimeMillis() - planFile.lastModified() > explainFreq) {

            if (!connDBName.equals(databaseName)) {
                plan = "You are connected to database " + connDBName + " while query " + sqlId + " executed in database " + databaseName + ".\nSorry.";
            } else if(query_text.contains("= $")||query_text.toLowerCase().contains("values ($")||query_text.toLowerCase().contains("in ($")||query_text.toLowerCase().contains("like $")) {
                plan = "It is probably a prepared query, we can not explain it. \nSorry.";
            } else {
                ResultSet rs1 = null;
                PreparedStatement st1 = null;
                try {
                    st1 = conn.prepareStatement("EXPLAIN " + query_text);
                    st1.setQueryTimeout(1);
                    rs1 = st1.executeQuery();
                } catch (Exception e) {
                    plan = plan + e.toString();
                }

                if (rs1 != null) {
                    try {
                        while (rs1.next()) {
                            plan = plan + rs1.getString(1) + "\n";
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    try {
                        rs1.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                if (st1 != null) {
                    try {
                        st1.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                if(plan != null && !plan.equals("")) {
		    planHashValue = PlanHashValue(plan);
                }
            }

            writePlan(sqlId, planHashValue, plan, planFileName);
            writeText(sqlId, query_text, textFileName);
        }
    }

    public static String readPlan(String sqlId) {

        String SQLPLAN = "";
	String planHashValue = "";
	String planFileName = Options.getInstance().getPlanDir() + System.getProperty("file.separator") + sqlId + ".plan";

        try {
	    File f = new File(planFileName);
	    if(f.exists() && !f.isDirectory()) {
		try (BufferedReader br = new BufferedReader(new FileReader(planFileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] plan2 = line.split("\\|", -1);
				planHashValue = plan2[0];

				SQLPLAN = SQLPLAN + "PLAN " + planHashValue + " FOR SQLID " + sqlId + ":\n"
				+ "-------------------------------------------------\n";

				for (int i = 1; i < plan2.length; i++) {
					SQLPLAN = SQLPLAN + plan2[i] + "\n";
				}
				SQLPLAN = SQLPLAN + "\n";
			}
		}
            } else {
                SQLPLAN = "";
            }
        } catch (Exception e) {
            System.out.println("Exception occured: " + e.getMessage());
            SQLPLAN = "";
        }
        return SQLPLAN;

	/*
	plan = "PLAN " + planHashValue + " FOR SQLID " + sqlId + " (" + command_type + "):\n"
	+ "-------------------------------------------------\n" + plan;
	*/
    }

    public static void writePlan(String sqlId, String planHashValue, String plan, String planFileName) {

	    File f = new File(planFileName);
	    if(f.exists() && !f.isDirectory()) { 
		try (BufferedReader br = new BufferedReader(new FileReader(planFileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] plan2 = line.split("\\|", -1);
				if(planHashValue.equals(plan2[0])) return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
            }

        plan = plan.replaceAll("\\n", "|");
	plan = planHashValue + "|" + plan + "\n";

        // Writer writer = null;
        BufferedWriter writer = null;
        try {
	    writer = new BufferedWriter(new FileWriter(planFileName, true));
            writer.append(plan);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeText(String sqlId, String query_text, String textFileName) {
        // Writer writer = null;
        BufferedWriter writer = null;
        try {
	    writer = new BufferedWriter(new FileWriter(textFileName, false));
            writer.write(query_text);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
