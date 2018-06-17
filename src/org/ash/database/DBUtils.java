/*
 *-------------------
 * The DBUtils.java is part of PASH Viewer
 *-------------------
 * 
 * ASH Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ASH Viewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ASH Viewer.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2009, Alex Kardapolov, All rights reserved.
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
            } else if (array[i].equals("insert")) {
                return "INSERT";
            } else if (array[i].equals("update")) {
                return "UPDATE";
            } else if (array[i].equals("insert")) {
                return "INSERT";
            } else if (array[i].equals("delete")) {
                return "DELETE";
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
            } else if (array[i].equals("autovacuum:")) {
                return "AUTOVACUUM";
            } else if (array[i].equals("analyze")) {
                return "ANALYZE";
            } else if (array[i].equals("explain")) {
                return "EXPLAIN";
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
            for (int j = i; j < array.length; j++) normalized_sql += array[j] + " ";
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
        } else if (array[i].equals("cost")||array[i].equals("rows")||array[i].equals("width")) {
            if(array[i+1].equals("="))
            {
                var_number++;
                array[i+2] = "$" + var_number;
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



    public static void explainPlan(String sqlId, String query_text, String command_type, String planDir, String fileSeparator, String connDBName, String databaseName, Connection conn) {

                        String planFileName = planDir + fileSeparator + sqlId + ".plan";
                        String textFileName = planDir + fileSeparator + sqlId + ".sql";
                        File planFile = new File(planFileName);
			String plan = "";
			String planHashValue = "";

                        // если mtime файла старше часа - запрашиваем план заново
                        if (System.currentTimeMillis() - planFile.lastModified() > 3600000) {

                            if (connDBName.equals(databaseName)) {

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
				    } catch (SQLException e) { e.printStackTrace(); }

				    try {
                                    rs1.close();
				    } catch (SQLException e) { e.printStackTrace(); }

				    planHashValue = PlanHashValue(plan);

				    plan = "PLAN " + planHashValue + " FOR SQLID " + sqlId + " (" + command_type + "):\n"
                                    + "------------------------------------------------------------\n\n" + plan;
                                }

                                if (st1 != null) {
				    try {
                                    st1.close();
				    } catch (SQLException e) { e.printStackTrace(); }
                                }
                            } else {
                                plan = plan + "You are connected to database " + connDBName + " while query " + sqlId + " executed in database " + databaseName;
                                plan = plan + ".\nSo sorry.";
                            }

                                Writer writer = null;
                                try {
                                    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(planFileName), "utf-8"));
                                    writer.write(plan);
                                } catch (IOException e) {
				    e.printStackTrace();
                                } finally {
                                    try {
                                        writer.close();
                                    } catch (Exception e) {
					e.printStackTrace();
				    }
                                }

                                writer = null;
                                try {
                                    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(textFileName), "utf-8"));
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

	
}
