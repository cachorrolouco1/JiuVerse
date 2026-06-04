package com.example.architecture.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- entities ---

@Entity(tableName = "player_memories")
data class PlayerMemoryEntity(
    @PrimaryKey val id: String = "local_player",
    val playerName: String = "Guerreiro Angra",
    val playerBelt: String = "Faixa Branca",
    val favoriteStyle: String = "Guarda Fechada",
    val completedQuestsCount: Int = 0,
    val lastVisitedRegion: String = "Arpoador Beach Dojo",
    val lastInteractionTime: Long = System.currentTimeMillis(),
    val totalTrainingMinutes: Int = 12,
    val masterReputation: Int = 10,
    val customNotes: String = "Iniciante promissor que foca na postura defensiva.",
    val playerLevel: Int = 25,
    val playerXp: Int = 1250,
    val academyName: String = "Alliance Itaim Bibi"
)

@Entity(tableName = "sensei_chat_messages")
data class SenseiChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "player", "sensei"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isVoiceMessage: Boolean = false,
    val localizedTopic: String = "General"
)

@Entity(tableName = "real_academies")
data class RealAcademyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val cnpj: String,
    val responsibleMaster: String,
    val region: String,
    val phone: String,
    val isVerified: Boolean = false,
    val verificationDocUrl: String = "",
    val memberCount: Int = 10,
    val jiuCoinsBalance: Int = 1200,
    val realRankPoints: Int = 450,
    val virtualRankPoints: Int = 680,
    val monetizationPlan: String = "Plano Inicial", // "Plano Inicial", "Premium Dojo", "Franquia Master"
    val monetizationPrice: Int = 99,
    val virtualGuildSynced: String = "Alliance Virtual"
)

@Entity(tableName = "gym_students")
data class GymStudentEntity(
    @PrimaryKey(autoGenerate = true) val studentId: Int = 0,
    val academyId: Int,
    val name: String,
    val belt: String,
    val registrationApproved: Boolean = false,
    val virtualNickname: String,
    val registrationDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "academy_tournaments")
data class AcademyTournamentEntity(
    @PrimaryKey(autoGenerate = true) val eventId: Int = 0,
    val academyId: Int,
    val title: String,
    val eventType: String, // "Campeonato Real", "Seminário", "Desafio Integrado"
    val entryFeeBrl: Int,
    val virtualSyncBonus: Int, // e.g. +200 JiuCoins
    val eventDate: String,
    val status: String = "Agendado" // "Agendado", "Minimizado", "Encerrado"
)

// --- DAOs ---

@Dao
interface PlayerMemoryDao {
    @Query("SELECT * FROM player_memories WHERE id = :id LIMIT 1")
    fun getPlayerMemory(id: String = "local_player"): Flow<PlayerMemoryEntity?>

    @Query("SELECT * FROM player_memories WHERE id = :id LIMIT 1")
    suspend fun getPlayerMemoryDirect(id: String = "local_player"): PlayerMemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePlayerMemory(memory: PlayerMemoryEntity)
}

@Dao
interface SenseiChatMessageDao {
    @Query("SELECT * FROM sensei_chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<SenseiChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SenseiChatMessageEntity)

    @Query("DELETE FROM sensei_chat_messages")
    suspend fun clearHistory()
}

@Dao
interface RealAcademyDao {
    @Query("SELECT * FROM real_academies ORDER BY realRankPoints DESC")
    fun getAllAcademies(): Flow<List<RealAcademyEntity>>

    @Query("SELECT * FROM real_academies WHERE id = :id LIMIT 1")
    suspend fun getAcademyById(id: Int): RealAcademyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademy(academy: RealAcademyEntity)

    @Update
    suspend fun updateAcademy(academy: RealAcademyEntity)

    @Delete
    suspend fun deleteAcademy(academy: RealAcademyEntity)
}

@Dao
interface GymStudentDao {
    @Query("SELECT * FROM gym_students WHERE academyId = :academyId ORDER BY name ASC")
    fun getStudentsForAcademy(academyId: Int): Flow<List<GymStudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: GymStudentEntity)

    @Query("DELETE FROM gym_students WHERE studentId = :studentId")
    suspend fun deleteStudent(studentId: Int)

    @Query("UPDATE gym_students SET registrationApproved = :approved WHERE studentId = :studentId")
    suspend fun updateStudentApproval(studentId: Int, approved: Boolean)
}

@Dao
interface AcademyTournamentDao {
    @Query("SELECT * FROM academy_tournaments ORDER BY eventId DESC")
    fun getAllTournaments(): Flow<List<AcademyTournamentEntity>>

    @Query("SELECT * FROM academy_tournaments WHERE academyId = :academyId")
    fun getTournamentsForAcademy(academyId: Int): Flow<List<AcademyTournamentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: AcademyTournamentEntity)

    @Query("DELETE FROM academy_tournaments WHERE eventId = :eventId")
    suspend fun deleteTournament(eventId: Int)
}

// --- Database Class ---

@Database(
    entities = [
        PlayerMemoryEntity::class, 
        SenseiChatMessageEntity::class,
        RealAcademyEntity::class,
        GymStudentEntity::class,
        AcademyTournamentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SenseiRoomDatabase : RoomDatabase() {
    abstract fun playerMemoryDao(): PlayerMemoryDao
    abstract fun senseiChatMessageDao(): SenseiChatMessageDao
    abstract fun realAcademyDao(): RealAcademyDao
    abstract fun gymStudentDao(): GymStudentDao
    abstract fun academyTournamentDao(): AcademyTournamentDao

    companion object {
        @Volatile
        private var INSTANCE: SenseiRoomDatabase? = null

        fun getDatabase(context: Context): SenseiRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SenseiRoomDatabase::class.java,
                    "sensei_jiuverse_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- Repository Module (ALWAYS use Repository Pattern) ---

class SenseiRepository(private val db: SenseiRoomDatabase) {
    private val playerMemoryDao = db.playerMemoryDao()
    private val senseiChatMessageDao = db.senseiChatMessageDao()
    private val realAcademyDao = db.realAcademyDao()
    private val gymStudentDao = db.gymStudentDao()
    private val academyTournamentDao = db.academyTournamentDao()

    val playerMemory: Flow<PlayerMemoryEntity?> = playerMemoryDao.getPlayerMemory()
    val chatHistory: Flow<List<SenseiChatMessageEntity>> = senseiChatMessageDao.getAllMessages()
    val allAcademies: Flow<List<RealAcademyEntity>> = realAcademyDao.getAllAcademies()
    val allTournaments: Flow<List<AcademyTournamentEntity>> = academyTournamentDao.getAllTournaments()

    suspend fun getPlayerMemoryDirect(): PlayerMemoryEntity {
        return playerMemoryDao.getPlayerMemoryDirect() ?: PlayerMemoryEntity()
    }

    suspend fun savePlayerMemory(memory: PlayerMemoryEntity) {
        playerMemoryDao.insertOrUpdatePlayerMemory(memory)
    }

    suspend fun addChatMessage(sender: String, content: String, isVoice: Boolean = false, topic: String = "General") {
        senseiChatMessageDao.insertMessage(
            SenseiChatMessageEntity(
                sender = sender,
                content = content,
                isVoiceMessage = isVoice,
                localizedTopic = topic
            )
        )
    }

    suspend fun clearChatHistory() {
        senseiChatMessageDao.clearHistory()
    }

    // --- Real Academies Integration Queries ---
    
    suspend fun saveAcademy(academy: RealAcademyEntity) {
        realAcademyDao.insertAcademy(academy)
    }

    suspend fun updateAcademy(academy: RealAcademyEntity) {
        realAcademyDao.updateAcademy(academy)
    }

    suspend fun deleteAcademy(academy: RealAcademyEntity) {
        realAcademyDao.deleteAcademy(academy)
    }

    fun getStudentsForAcademy(academyId: Int): Flow<List<GymStudentEntity>> {
        return gymStudentDao.getStudentsForAcademy(academyId)
    }

    suspend fun enrollStudent(student: GymStudentEntity) {
        gymStudentDao.insertStudent(student)
    }

    suspend fun deleteStudent(studentId: Int) {
        gymStudentDao.deleteStudent(studentId)
    }

    suspend fun verifyStudent(studentId: Int, approved: Boolean) {
        gymStudentDao.updateStudentApproval(studentId, approved)
    }

    fun getTournamentsForAcademy(academyId: Int): Flow<List<AcademyTournamentEntity>> {
        return academyTournamentDao.getTournamentsForAcademy(academyId)
    }

    suspend fun addTournament(tournament: AcademyTournamentEntity) {
        academyTournamentDao.insertTournament(tournament)
    }

    suspend fun deleteTournament(eventId: Int) {
        academyTournamentDao.deleteTournament(eventId)
    }
}
