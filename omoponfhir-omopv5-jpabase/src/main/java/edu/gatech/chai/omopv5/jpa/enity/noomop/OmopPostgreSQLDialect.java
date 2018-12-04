package edu.gatech.chai.omopv5.jpa.enity.noomop;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQL94Dialect;

public class OmopPostgreSQLDialect extends PostgreSQL94Dialect {
	public OmopPostgreSQLDialect() {
		this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
	}
}
