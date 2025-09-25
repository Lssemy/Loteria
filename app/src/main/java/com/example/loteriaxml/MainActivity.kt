package com.example.loteriaxml

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var bancoDados: SQLiteDatabase
    private lateinit var listViewDados: ListView
    private lateinit var txtNumeros: TextView
    private lateinit var btnGerar: Button
    private lateinit var btnListar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtNumeros = findViewById(R.id.txtNumeros)
        btnGerar = findViewById(R.id.btnGerar)
        btnListar = findViewById(R.id.btnListar)
        listViewDados = findViewById(R.id.listViewDados)

        criarBancoDados()

        btnGerar.setOnClickListener {
            val numeros = sortearNumeros()
            val formatados = numeros.joinToString(" - ") { n -> "%02d".format(n) }
            txtNumeros.text = "Números: $formatados"
            inserirDados(formatados)
        }

        btnListar.setOnClickListener {
            listarDados()
        }
    }

    // Criar banco
    private fun criarBancoDados() {
        try {
            bancoDados = openOrCreateDatabase("loteria.db", MODE_PRIVATE, null)
            bancoDados.execSQL(
                "CREATE TABLE IF NOT EXISTS sorteios (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "numeros TEXT)"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Sorteio 6 números
    private fun sortearNumeros(): List<Int> {
        val numeros = mutableSetOf<Int>()
        while (numeros.size < 6) {
            numeros.add(Random.nextInt(1, 61))
        }
        return numeros.sorted()
    }

    // Inserir no banco
    private fun inserirDados(numeros: String) {
        try {
            bancoDados = openOrCreateDatabase("loteria.db", MODE_PRIVATE, null)
            val sql = "INSERT INTO sorteios (numeros) VALUES (?)"
            val stmt: SQLiteStatement = bancoDados.compileStatement(sql)
            stmt.bindString(1, numeros)
            stmt.executeInsert()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bancoDados.close()
        }
    }

    // Listar histórico
    private fun listarDados() {
        try {
            bancoDados = openOrCreateDatabase("loteria.db", MODE_PRIVATE, null)
            val cursor = bancoDados.rawQuery("SELECT * FROM sorteios ORDER BY id DESC", null)

            val dados = ArrayList<String>()
            if (cursor.moveToFirst()) {
                do {
                    val numeros = cursor.getString(cursor.getColumnIndexOrThrow("numeros"))
                    dados.add(numeros)
                } while (cursor.moveToNext())
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dados)
            listViewDados.adapter = adapter

            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bancoDados.close()
        }
    }
}
