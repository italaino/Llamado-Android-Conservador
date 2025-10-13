package cl.gpv.llamado_conservador.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "FirestoreRepo"

data class Ticket(
    val seccion: String = "",
    val ticket: String = "",
    val llamados: Int = 0,
    val timestamp: Timestamp? = null,
    val modulo: String = ""
)

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    // Aliases legibles según tus IDs reales (los de la captura)
    private val SECTION_LABELS = mapOf(
        "general" to "Atención General",
        "preferencial" to "Atención Preferencial",
        "retiro_docs" to "Retiro de Documentos",
        "revision_libros" to "Revisión de Libros"
    )

    /** Último ticket: escucha TODOS los docs de 'ultimos_piso8' y toma el más reciente por timestamp. */
    fun getUltimo(): Flow<Ticket?> = callbackFlow {
        Log.d(TAG, "Escuchando colección ultimos_piso8 …")
        val ref = db.collection("ultimos_piso8")

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error ultimos_piso8: ", error)
                trySend(null); return@addSnapshotListener
            }
            if (snapshot == null) {
                Log.w(TAG, "ultimos_piso8 snapshot == null")
                trySend(null); return@addSnapshotListener
            }
            if (snapshot.isEmpty) {
                Log.w(TAG, "ultimos_piso8 vacío")
                trySend(null); return@addSnapshotListener
            }

            val all = snapshot.documents.mapNotNull { doc ->
                val seccionKey = doc.id
                val seccionField = doc.getString("seccion")
                val seccion = when {
                    !seccionField.isNullOrBlank() ->
                        SECTION_LABELS[seccionField] ?: seccionField.replace('_', ' ').uppercase()
                    else ->
                        SECTION_LABELS[seccionKey] ?: seccionKey.replace('_', ' ').uppercase()
                }

                val ticket = doc.getString("ticket") ?: return@mapNotNull null
                val llamados = (doc.getLong("llamados") ?: 0L).toInt()
                val timestamp = doc.getTimestamp("timestamp")
                val modulo = doc.getString("modulo") ?: ""

                Ticket(
                    seccion = seccion,
                    ticket = ticket,
                    llamados = llamados,
                    timestamp = timestamp,
                    modulo = modulo
                )
            }

            val ultimo = all.maxByOrNull { it.timestamp?.toDate()?.time ?: 0L }
            Log.d(TAG, "ultimos_piso8 -> size=${all.size} | ultimo=${ultimo?.ticket} @${ultimo?.timestamp}")
            trySend(ultimo)
        }

        awaitClose { listener.remove() }
    }

    /** Historial: lee desde 'historial_piso8' (4 últimos por timestamp DESC). */
    fun getHistorial(limit: Long = 4): Flow<List<Ticket>> = callbackFlow {
        Log.d(TAG, "Escuchando colección historial_piso8 …")
        val ref = db.collection("historial_piso8")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error historial_piso8: ", error)
                trySend(emptyList()); return@addSnapshotListener
            }
            if (snapshot == null) {
                Log.w(TAG, "historial_piso8 snapshot == null")
                trySend(emptyList()); return@addSnapshotListener
            }
            if (snapshot.isEmpty) {
                Log.w(TAG, "historial_piso8 vacío")
                trySend(emptyList()); return@addSnapshotListener
            }

            val items = snapshot.documents.mapNotNull { doc ->
                val seccionKey = doc.getString("seccion") ?: doc.getString("section") ?: return@mapNotNull null
                val ticket = doc.getString("ticket") ?: return@mapNotNull null
                val timestamp = doc.getTimestamp("timestamp")
                val modulo = doc.getString("modulo") ?: ""

                Ticket(
                    seccion = SECTION_LABELS[seccionKey] ?: seccionKey.replace('_', ' ').uppercase(),
                    ticket = ticket,
                    llamados = 0,
                    timestamp = timestamp,
                    modulo = modulo
                )
            }
            Log.d(TAG, "historial_piso8 -> items=${items.size} | top=${items.firstOrNull()?.ticket}")
            trySend(items)
        }

        awaitClose { listener.remove() }
    }
}
