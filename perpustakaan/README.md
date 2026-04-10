## Perpustakaan (Java + Maven)

Proyek ini adalah aplikasi manajemen perpustakaan berbasis Java dengan database MySQL.

Struktur proyek mengikuti standar Maven:

- src/main/java untuk source code Java
- src/main/resources untuk file resource seperti db.properties
- database untuk schema dan seed SQL

## Prasyarat

- JDK 17+
- Maven 3.8+
- MySQL server aktif

## Konfigurasi Database

Atur kredensial database di file src/main/resources/db.properties.

## Build

Jalankan dari root folder proyek:

	mvn clean compile

Untuk build artefak:

	mvn clean package

## Menjalankan Aplikasi

Entry point aplikasi ada di class com.library.app.Application.

Jalankan dengan Maven:

	mvn exec:java

Jika ingin override konfigurasi database langsung dari command line:

	mvn exec:java -Ddb.host=127.0.0.1 -Ddb.port=3306 -Ddb.name=library_management_native_maven -Ddb.username=root -Ddb.password=PASSWORD
