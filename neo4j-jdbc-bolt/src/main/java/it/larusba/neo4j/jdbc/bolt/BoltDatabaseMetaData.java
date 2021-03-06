/**
 * Copyright (c) 2016 LARUS Business Automation [http://www.larus-ba.it]
 * <p>
 * This file is part of the "LARUS Integration Framework for Neo4j".
 * <p>
 * The "LARUS Integration Framework for Neo4j" is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Created on 01/03/2016
 */
package it.larusba.neo4j.jdbc.bolt;

import it.larusba.neo4j.jdbc.DatabaseMetaData;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides metadata
 *
 * @author AgileLARUS
 * @since 3.0.0
 */
public class BoltDatabaseMetaData extends DatabaseMetaData {

	private String databaseVersion = "Unknown";
	private BoltConnection connection;
	private boolean debug = false;

	public BoltDatabaseMetaData(BoltConnection connection, boolean debug) {
		super();
		this.connection = connection;
		this.debug = debug;

		// compute database version
		if (connection != null && connection.getSession() != null) {
			try(Transaction tx = connection.getSession().beginTransaction()) {
				StatementResult rs = tx.run("CALL dbms.components() yield name,versions WITH * WHERE name=\"Neo4j Kernel\" RETURN versions[0] AS version");
				if (rs != null && rs.hasNext()) {
					Record record = rs.next();
					if (record.containsKey("version")) {
						databaseVersion = record.get("version").asString();
					}
				}
			}
		}

	}

	public BoltDatabaseMetaData(BoltConnection connection) {
		this(connection, false);
	}

	@Override public Connection getConnection() throws SQLException {
		return this.connection;
	}

	@Override public String getDatabaseProductVersion() throws SQLException {
		return databaseVersion;
	}

	@Override public int getDatabaseMajorVersion() {
		return extractVersionPart(databaseVersion, 1);
	}

	@Override public int getDatabaseMinorVersion() {
		return extractVersionPart(databaseVersion, 2);
	}
}
