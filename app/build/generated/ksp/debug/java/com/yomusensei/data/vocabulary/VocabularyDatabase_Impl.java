package com.yomusensei.data.vocabulary;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class VocabularyDatabase_Impl extends VocabularyDatabase {
  private volatile VocabularyDao _vocabularyDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `vocabulary` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT NOT NULL, `reading` TEXT, `meaning` TEXT NOT NULL, `explanation` TEXT NOT NULL, `partOfSpeech` TEXT, `category` TEXT, `sourceArticleTitle` TEXT, `sourceArticleUrl` TEXT, `addedTime` INTEGER NOT NULL, `isManuallyAdded` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, `reviewCount` INTEGER NOT NULL, `correctCount` INTEGER NOT NULL, `lastReviewTime` INTEGER, `nextReviewTime` INTEGER, `reviewLevel` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `word_tags` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `wordId` INTEGER NOT NULL, `tag` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `review_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `wordId` INTEGER NOT NULL, `reviewTime` INTEGER NOT NULL, `isCorrect` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_questions` (`wordId` INTEGER NOT NULL, `distractors` TEXT NOT NULL, `generatedTime` INTEGER NOT NULL, `expiresAt` INTEGER NOT NULL, PRIMARY KEY(`wordId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '48a7dcdb13b90d2a7f2bd5ae7a4176d4')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `vocabulary`");
        db.execSQL("DROP TABLE IF EXISTS `word_tags`");
        db.execSQL("DROP TABLE IF EXISTS `review_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `cached_questions`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsVocabulary = new HashMap<String, TableInfo.Column>(17);
        _columnsVocabulary.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("word", new TableInfo.Column("word", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("reading", new TableInfo.Column("reading", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("meaning", new TableInfo.Column("meaning", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("explanation", new TableInfo.Column("explanation", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("partOfSpeech", new TableInfo.Column("partOfSpeech", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("sourceArticleTitle", new TableInfo.Column("sourceArticleTitle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("sourceArticleUrl", new TableInfo.Column("sourceArticleUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("addedTime", new TableInfo.Column("addedTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("isManuallyAdded", new TableInfo.Column("isManuallyAdded", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("reviewCount", new TableInfo.Column("reviewCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("correctCount", new TableInfo.Column("correctCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("lastReviewTime", new TableInfo.Column("lastReviewTime", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("nextReviewTime", new TableInfo.Column("nextReviewTime", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVocabulary.put("reviewLevel", new TableInfo.Column("reviewLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysVocabulary = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesVocabulary = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoVocabulary = new TableInfo("vocabulary", _columnsVocabulary, _foreignKeysVocabulary, _indicesVocabulary);
        final TableInfo _existingVocabulary = TableInfo.read(db, "vocabulary");
        if (!_infoVocabulary.equals(_existingVocabulary)) {
          return new RoomOpenHelper.ValidationResult(false, "vocabulary(com.yomusensei.data.vocabulary.VocabularyWord).\n"
                  + " Expected:\n" + _infoVocabulary + "\n"
                  + " Found:\n" + _existingVocabulary);
        }
        final HashMap<String, TableInfo.Column> _columnsWordTags = new HashMap<String, TableInfo.Column>(3);
        _columnsWordTags.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordTags.put("wordId", new TableInfo.Column("wordId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordTags.put("tag", new TableInfo.Column("tag", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWordTags = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWordTags = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWordTags = new TableInfo("word_tags", _columnsWordTags, _foreignKeysWordTags, _indicesWordTags);
        final TableInfo _existingWordTags = TableInfo.read(db, "word_tags");
        if (!_infoWordTags.equals(_existingWordTags)) {
          return new RoomOpenHelper.ValidationResult(false, "word_tags(com.yomusensei.data.vocabulary.WordTag).\n"
                  + " Expected:\n" + _infoWordTags + "\n"
                  + " Found:\n" + _existingWordTags);
        }
        final HashMap<String, TableInfo.Column> _columnsReviewSessions = new HashMap<String, TableInfo.Column>(4);
        _columnsReviewSessions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReviewSessions.put("wordId", new TableInfo.Column("wordId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReviewSessions.put("reviewTime", new TableInfo.Column("reviewTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReviewSessions.put("isCorrect", new TableInfo.Column("isCorrect", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReviewSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReviewSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReviewSessions = new TableInfo("review_sessions", _columnsReviewSessions, _foreignKeysReviewSessions, _indicesReviewSessions);
        final TableInfo _existingReviewSessions = TableInfo.read(db, "review_sessions");
        if (!_infoReviewSessions.equals(_existingReviewSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "review_sessions(com.yomusensei.data.vocabulary.ReviewSession).\n"
                  + " Expected:\n" + _infoReviewSessions + "\n"
                  + " Found:\n" + _existingReviewSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedQuestions = new HashMap<String, TableInfo.Column>(4);
        _columnsCachedQuestions.put("wordId", new TableInfo.Column("wordId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedQuestions.put("distractors", new TableInfo.Column("distractors", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedQuestions.put("generatedTime", new TableInfo.Column("generatedTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedQuestions.put("expiresAt", new TableInfo.Column("expiresAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedQuestions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedQuestions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedQuestions = new TableInfo("cached_questions", _columnsCachedQuestions, _foreignKeysCachedQuestions, _indicesCachedQuestions);
        final TableInfo _existingCachedQuestions = TableInfo.read(db, "cached_questions");
        if (!_infoCachedQuestions.equals(_existingCachedQuestions)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_questions(com.yomusensei.data.vocabulary.CachedQuestion).\n"
                  + " Expected:\n" + _infoCachedQuestions + "\n"
                  + " Found:\n" + _existingCachedQuestions);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "48a7dcdb13b90d2a7f2bd5ae7a4176d4", "ad5742a855a8bfec27ce596a44e3a580");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "vocabulary","word_tags","review_sessions","cached_questions");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `vocabulary`");
      _db.execSQL("DELETE FROM `word_tags`");
      _db.execSQL("DELETE FROM `review_sessions`");
      _db.execSQL("DELETE FROM `cached_questions`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(VocabularyDao.class, VocabularyDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public VocabularyDao vocabularyDao() {
    if (_vocabularyDao != null) {
      return _vocabularyDao;
    } else {
      synchronized(this) {
        if(_vocabularyDao == null) {
          _vocabularyDao = new VocabularyDao_Impl(this);
        }
        return _vocabularyDao;
      }
    }
  }
}
