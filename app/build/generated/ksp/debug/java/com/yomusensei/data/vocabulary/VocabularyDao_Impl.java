package com.yomusensei.data.vocabulary;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class VocabularyDao_Impl implements VocabularyDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<VocabularyWord> __insertionAdapterOfVocabularyWord;

  private final EntityInsertionAdapter<WordTag> __insertionAdapterOfWordTag;

  private final EntityInsertionAdapter<CachedQuestion> __insertionAdapterOfCachedQuestion;

  private final StringListConverter __stringListConverter = new StringListConverter();

  private final EntityInsertionAdapter<ReviewSession> __insertionAdapterOfReviewSession;

  private final EntityDeletionOrUpdateAdapter<VocabularyWord> __deletionAdapterOfVocabularyWord;

  private final EntityDeletionOrUpdateAdapter<VocabularyWord> __updateAdapterOfVocabularyWord;

  private final SharedSQLiteStatement __preparedStmtOfDeleteTagsForWord;

  private final SharedSQLiteStatement __preparedStmtOfDeleteTag;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpiredQuestions;

  public VocabularyDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfVocabularyWord = new EntityInsertionAdapter<VocabularyWord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `vocabulary` (`id`,`word`,`reading`,`meaning`,`explanation`,`partOfSpeech`,`category`,`sourceArticleTitle`,`sourceArticleUrl`,`addedTime`,`isManuallyAdded`,`isFavorite`,`reviewCount`,`correctCount`,`lastReviewTime`,`nextReviewTime`,`reviewLevel`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VocabularyWord entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getWord());
        if (entity.getReading() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getReading());
        }
        statement.bindString(4, entity.getMeaning());
        statement.bindString(5, entity.getExplanation());
        if (entity.getPartOfSpeech() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPartOfSpeech());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCategory());
        }
        if (entity.getSourceArticleTitle() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSourceArticleTitle());
        }
        if (entity.getSourceArticleUrl() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getSourceArticleUrl());
        }
        statement.bindLong(10, entity.getAddedTime());
        final int _tmp = entity.isManuallyAdded() ? 1 : 0;
        statement.bindLong(11, _tmp);
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        statement.bindLong(13, entity.getReviewCount());
        statement.bindLong(14, entity.getCorrectCount());
        if (entity.getLastReviewTime() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getLastReviewTime());
        }
        if (entity.getNextReviewTime() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getNextReviewTime());
        }
        statement.bindLong(17, entity.getReviewLevel());
      }
    };
    this.__insertionAdapterOfWordTag = new EntityInsertionAdapter<WordTag>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `word_tags` (`id`,`wordId`,`tag`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WordTag entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getWordId());
        statement.bindString(3, entity.getTag());
      }
    };
    this.__insertionAdapterOfCachedQuestion = new EntityInsertionAdapter<CachedQuestion>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cached_questions` (`wordId`,`distractors`,`generatedTime`,`expiresAt`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CachedQuestion entity) {
        statement.bindLong(1, entity.getWordId());
        final String _tmp = __stringListConverter.toString(entity.getDistractors());
        statement.bindString(2, _tmp);
        statement.bindLong(3, entity.getGeneratedTime());
        statement.bindLong(4, entity.getExpiresAt());
      }
    };
    this.__insertionAdapterOfReviewSession = new EntityInsertionAdapter<ReviewSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `review_sessions` (`id`,`wordId`,`reviewTime`,`isCorrect`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReviewSession entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getWordId());
        statement.bindLong(3, entity.getReviewTime());
        final int _tmp = entity.isCorrect() ? 1 : 0;
        statement.bindLong(4, _tmp);
      }
    };
    this.__deletionAdapterOfVocabularyWord = new EntityDeletionOrUpdateAdapter<VocabularyWord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `vocabulary` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VocabularyWord entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfVocabularyWord = new EntityDeletionOrUpdateAdapter<VocabularyWord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `vocabulary` SET `id` = ?,`word` = ?,`reading` = ?,`meaning` = ?,`explanation` = ?,`partOfSpeech` = ?,`category` = ?,`sourceArticleTitle` = ?,`sourceArticleUrl` = ?,`addedTime` = ?,`isManuallyAdded` = ?,`isFavorite` = ?,`reviewCount` = ?,`correctCount` = ?,`lastReviewTime` = ?,`nextReviewTime` = ?,`reviewLevel` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VocabularyWord entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getWord());
        if (entity.getReading() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getReading());
        }
        statement.bindString(4, entity.getMeaning());
        statement.bindString(5, entity.getExplanation());
        if (entity.getPartOfSpeech() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPartOfSpeech());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCategory());
        }
        if (entity.getSourceArticleTitle() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSourceArticleTitle());
        }
        if (entity.getSourceArticleUrl() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getSourceArticleUrl());
        }
        statement.bindLong(10, entity.getAddedTime());
        final int _tmp = entity.isManuallyAdded() ? 1 : 0;
        statement.bindLong(11, _tmp);
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        statement.bindLong(13, entity.getReviewCount());
        statement.bindLong(14, entity.getCorrectCount());
        if (entity.getLastReviewTime() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getLastReviewTime());
        }
        if (entity.getNextReviewTime() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getNextReviewTime());
        }
        statement.bindLong(17, entity.getReviewLevel());
        statement.bindLong(18, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteTagsForWord = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM word_tags WHERE wordId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteTag = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM word_tags WHERE wordId = ? AND tag = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteExpiredQuestions = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_questions WHERE expiresAt < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertWord(final VocabularyWord word,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfVocabularyWord.insertAndReturnId(word);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertTag(final WordTag tag, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWordTag.insert(tag);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object cacheQuestion(final CachedQuestion question,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCachedQuestion.insert(question);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertReviewSession(final ReviewSession session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfReviewSession.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteWord(final VocabularyWord word,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfVocabularyWord.handle(word);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateWord(final VocabularyWord word,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfVocabularyWord.handle(word);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTagsForWord(final long wordId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteTagsForWord.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, wordId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteTagsForWord.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTag(final long wordId, final String tag,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteTag.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, wordId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, tag);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteTag.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExpiredQuestions(final long currentTime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExpiredQuestions.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, currentTime);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteExpiredQuestions.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<VocabularyWord>> getAllWords() {
    final String _sql = "SELECT * FROM vocabulary ORDER BY addedTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vocabulary"}, new Callable<List<VocabularyWord>>() {
      @Override
      @NonNull
      public List<VocabularyWord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfReading = CursorUtil.getColumnIndexOrThrow(_cursor, "reading");
          final int _cursorIndexOfMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "meaning");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSourceArticleTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleTitle");
          final int _cursorIndexOfSourceArticleUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleUrl");
          final int _cursorIndexOfAddedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTime");
          final int _cursorIndexOfIsManuallyAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "isManuallyAdded");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final int _cursorIndexOfCorrectCount = CursorUtil.getColumnIndexOrThrow(_cursor, "correctCount");
          final int _cursorIndexOfLastReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReviewTime");
          final int _cursorIndexOfNextReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "nextReviewTime");
          final int _cursorIndexOfReviewLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewLevel");
          final List<VocabularyWord> _result = new ArrayList<VocabularyWord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VocabularyWord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpReading;
            if (_cursor.isNull(_cursorIndexOfReading)) {
              _tmpReading = null;
            } else {
              _tmpReading = _cursor.getString(_cursorIndexOfReading);
            }
            final String _tmpMeaning;
            _tmpMeaning = _cursor.getString(_cursorIndexOfMeaning);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpPartOfSpeech;
            if (_cursor.isNull(_cursorIndexOfPartOfSpeech)) {
              _tmpPartOfSpeech = null;
            } else {
              _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpSourceArticleTitle;
            if (_cursor.isNull(_cursorIndexOfSourceArticleTitle)) {
              _tmpSourceArticleTitle = null;
            } else {
              _tmpSourceArticleTitle = _cursor.getString(_cursorIndexOfSourceArticleTitle);
            }
            final String _tmpSourceArticleUrl;
            if (_cursor.isNull(_cursorIndexOfSourceArticleUrl)) {
              _tmpSourceArticleUrl = null;
            } else {
              _tmpSourceArticleUrl = _cursor.getString(_cursorIndexOfSourceArticleUrl);
            }
            final long _tmpAddedTime;
            _tmpAddedTime = _cursor.getLong(_cursorIndexOfAddedTime);
            final boolean _tmpIsManuallyAdded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManuallyAdded);
            _tmpIsManuallyAdded = _tmp != 0;
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            final int _tmpCorrectCount;
            _tmpCorrectCount = _cursor.getInt(_cursorIndexOfCorrectCount);
            final Long _tmpLastReviewTime;
            if (_cursor.isNull(_cursorIndexOfLastReviewTime)) {
              _tmpLastReviewTime = null;
            } else {
              _tmpLastReviewTime = _cursor.getLong(_cursorIndexOfLastReviewTime);
            }
            final Long _tmpNextReviewTime;
            if (_cursor.isNull(_cursorIndexOfNextReviewTime)) {
              _tmpNextReviewTime = null;
            } else {
              _tmpNextReviewTime = _cursor.getLong(_cursorIndexOfNextReviewTime);
            }
            final int _tmpReviewLevel;
            _tmpReviewLevel = _cursor.getInt(_cursorIndexOfReviewLevel);
            _item = new VocabularyWord(_tmpId,_tmpWord,_tmpReading,_tmpMeaning,_tmpExplanation,_tmpPartOfSpeech,_tmpCategory,_tmpSourceArticleTitle,_tmpSourceArticleUrl,_tmpAddedTime,_tmpIsManuallyAdded,_tmpIsFavorite,_tmpReviewCount,_tmpCorrectCount,_tmpLastReviewTime,_tmpNextReviewTime,_tmpReviewLevel);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getWordById(final long id, final Continuation<? super VocabularyWord> $completion) {
    final String _sql = "SELECT * FROM vocabulary WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<VocabularyWord>() {
      @Override
      @Nullable
      public VocabularyWord call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfReading = CursorUtil.getColumnIndexOrThrow(_cursor, "reading");
          final int _cursorIndexOfMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "meaning");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSourceArticleTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleTitle");
          final int _cursorIndexOfSourceArticleUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleUrl");
          final int _cursorIndexOfAddedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTime");
          final int _cursorIndexOfIsManuallyAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "isManuallyAdded");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final int _cursorIndexOfCorrectCount = CursorUtil.getColumnIndexOrThrow(_cursor, "correctCount");
          final int _cursorIndexOfLastReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReviewTime");
          final int _cursorIndexOfNextReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "nextReviewTime");
          final int _cursorIndexOfReviewLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewLevel");
          final VocabularyWord _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpReading;
            if (_cursor.isNull(_cursorIndexOfReading)) {
              _tmpReading = null;
            } else {
              _tmpReading = _cursor.getString(_cursorIndexOfReading);
            }
            final String _tmpMeaning;
            _tmpMeaning = _cursor.getString(_cursorIndexOfMeaning);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpPartOfSpeech;
            if (_cursor.isNull(_cursorIndexOfPartOfSpeech)) {
              _tmpPartOfSpeech = null;
            } else {
              _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpSourceArticleTitle;
            if (_cursor.isNull(_cursorIndexOfSourceArticleTitle)) {
              _tmpSourceArticleTitle = null;
            } else {
              _tmpSourceArticleTitle = _cursor.getString(_cursorIndexOfSourceArticleTitle);
            }
            final String _tmpSourceArticleUrl;
            if (_cursor.isNull(_cursorIndexOfSourceArticleUrl)) {
              _tmpSourceArticleUrl = null;
            } else {
              _tmpSourceArticleUrl = _cursor.getString(_cursorIndexOfSourceArticleUrl);
            }
            final long _tmpAddedTime;
            _tmpAddedTime = _cursor.getLong(_cursorIndexOfAddedTime);
            final boolean _tmpIsManuallyAdded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManuallyAdded);
            _tmpIsManuallyAdded = _tmp != 0;
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            final int _tmpCorrectCount;
            _tmpCorrectCount = _cursor.getInt(_cursorIndexOfCorrectCount);
            final Long _tmpLastReviewTime;
            if (_cursor.isNull(_cursorIndexOfLastReviewTime)) {
              _tmpLastReviewTime = null;
            } else {
              _tmpLastReviewTime = _cursor.getLong(_cursorIndexOfLastReviewTime);
            }
            final Long _tmpNextReviewTime;
            if (_cursor.isNull(_cursorIndexOfNextReviewTime)) {
              _tmpNextReviewTime = null;
            } else {
              _tmpNextReviewTime = _cursor.getLong(_cursorIndexOfNextReviewTime);
            }
            final int _tmpReviewLevel;
            _tmpReviewLevel = _cursor.getInt(_cursorIndexOfReviewLevel);
            _result = new VocabularyWord(_tmpId,_tmpWord,_tmpReading,_tmpMeaning,_tmpExplanation,_tmpPartOfSpeech,_tmpCategory,_tmpSourceArticleTitle,_tmpSourceArticleUrl,_tmpAddedTime,_tmpIsManuallyAdded,_tmpIsFavorite,_tmpReviewCount,_tmpCorrectCount,_tmpLastReviewTime,_tmpNextReviewTime,_tmpReviewLevel);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getWordByText(final String word,
      final Continuation<? super VocabularyWord> $completion) {
    final String _sql = "SELECT * FROM vocabulary WHERE word = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, word);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<VocabularyWord>() {
      @Override
      @Nullable
      public VocabularyWord call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfReading = CursorUtil.getColumnIndexOrThrow(_cursor, "reading");
          final int _cursorIndexOfMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "meaning");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSourceArticleTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleTitle");
          final int _cursorIndexOfSourceArticleUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleUrl");
          final int _cursorIndexOfAddedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTime");
          final int _cursorIndexOfIsManuallyAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "isManuallyAdded");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final int _cursorIndexOfCorrectCount = CursorUtil.getColumnIndexOrThrow(_cursor, "correctCount");
          final int _cursorIndexOfLastReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReviewTime");
          final int _cursorIndexOfNextReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "nextReviewTime");
          final int _cursorIndexOfReviewLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewLevel");
          final VocabularyWord _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpReading;
            if (_cursor.isNull(_cursorIndexOfReading)) {
              _tmpReading = null;
            } else {
              _tmpReading = _cursor.getString(_cursorIndexOfReading);
            }
            final String _tmpMeaning;
            _tmpMeaning = _cursor.getString(_cursorIndexOfMeaning);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpPartOfSpeech;
            if (_cursor.isNull(_cursorIndexOfPartOfSpeech)) {
              _tmpPartOfSpeech = null;
            } else {
              _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpSourceArticleTitle;
            if (_cursor.isNull(_cursorIndexOfSourceArticleTitle)) {
              _tmpSourceArticleTitle = null;
            } else {
              _tmpSourceArticleTitle = _cursor.getString(_cursorIndexOfSourceArticleTitle);
            }
            final String _tmpSourceArticleUrl;
            if (_cursor.isNull(_cursorIndexOfSourceArticleUrl)) {
              _tmpSourceArticleUrl = null;
            } else {
              _tmpSourceArticleUrl = _cursor.getString(_cursorIndexOfSourceArticleUrl);
            }
            final long _tmpAddedTime;
            _tmpAddedTime = _cursor.getLong(_cursorIndexOfAddedTime);
            final boolean _tmpIsManuallyAdded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManuallyAdded);
            _tmpIsManuallyAdded = _tmp != 0;
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            final int _tmpCorrectCount;
            _tmpCorrectCount = _cursor.getInt(_cursorIndexOfCorrectCount);
            final Long _tmpLastReviewTime;
            if (_cursor.isNull(_cursorIndexOfLastReviewTime)) {
              _tmpLastReviewTime = null;
            } else {
              _tmpLastReviewTime = _cursor.getLong(_cursorIndexOfLastReviewTime);
            }
            final Long _tmpNextReviewTime;
            if (_cursor.isNull(_cursorIndexOfNextReviewTime)) {
              _tmpNextReviewTime = null;
            } else {
              _tmpNextReviewTime = _cursor.getLong(_cursorIndexOfNextReviewTime);
            }
            final int _tmpReviewLevel;
            _tmpReviewLevel = _cursor.getInt(_cursorIndexOfReviewLevel);
            _result = new VocabularyWord(_tmpId,_tmpWord,_tmpReading,_tmpMeaning,_tmpExplanation,_tmpPartOfSpeech,_tmpCategory,_tmpSourceArticleTitle,_tmpSourceArticleUrl,_tmpAddedTime,_tmpIsManuallyAdded,_tmpIsFavorite,_tmpReviewCount,_tmpCorrectCount,_tmpLastReviewTime,_tmpNextReviewTime,_tmpReviewLevel);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<VocabularyWord>> searchWords(final String query) {
    final String _sql = "\n"
            + "        SELECT * FROM vocabulary\n"
            + "        WHERE word LIKE '%' || ? || '%'\n"
            + "        OR meaning LIKE '%' || ? || '%'\n"
            + "        ORDER BY addedTime DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vocabulary"}, new Callable<List<VocabularyWord>>() {
      @Override
      @NonNull
      public List<VocabularyWord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfReading = CursorUtil.getColumnIndexOrThrow(_cursor, "reading");
          final int _cursorIndexOfMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "meaning");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSourceArticleTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleTitle");
          final int _cursorIndexOfSourceArticleUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleUrl");
          final int _cursorIndexOfAddedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTime");
          final int _cursorIndexOfIsManuallyAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "isManuallyAdded");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final int _cursorIndexOfCorrectCount = CursorUtil.getColumnIndexOrThrow(_cursor, "correctCount");
          final int _cursorIndexOfLastReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReviewTime");
          final int _cursorIndexOfNextReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "nextReviewTime");
          final int _cursorIndexOfReviewLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewLevel");
          final List<VocabularyWord> _result = new ArrayList<VocabularyWord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VocabularyWord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpReading;
            if (_cursor.isNull(_cursorIndexOfReading)) {
              _tmpReading = null;
            } else {
              _tmpReading = _cursor.getString(_cursorIndexOfReading);
            }
            final String _tmpMeaning;
            _tmpMeaning = _cursor.getString(_cursorIndexOfMeaning);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpPartOfSpeech;
            if (_cursor.isNull(_cursorIndexOfPartOfSpeech)) {
              _tmpPartOfSpeech = null;
            } else {
              _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpSourceArticleTitle;
            if (_cursor.isNull(_cursorIndexOfSourceArticleTitle)) {
              _tmpSourceArticleTitle = null;
            } else {
              _tmpSourceArticleTitle = _cursor.getString(_cursorIndexOfSourceArticleTitle);
            }
            final String _tmpSourceArticleUrl;
            if (_cursor.isNull(_cursorIndexOfSourceArticleUrl)) {
              _tmpSourceArticleUrl = null;
            } else {
              _tmpSourceArticleUrl = _cursor.getString(_cursorIndexOfSourceArticleUrl);
            }
            final long _tmpAddedTime;
            _tmpAddedTime = _cursor.getLong(_cursorIndexOfAddedTime);
            final boolean _tmpIsManuallyAdded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManuallyAdded);
            _tmpIsManuallyAdded = _tmp != 0;
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            final int _tmpCorrectCount;
            _tmpCorrectCount = _cursor.getInt(_cursorIndexOfCorrectCount);
            final Long _tmpLastReviewTime;
            if (_cursor.isNull(_cursorIndexOfLastReviewTime)) {
              _tmpLastReviewTime = null;
            } else {
              _tmpLastReviewTime = _cursor.getLong(_cursorIndexOfLastReviewTime);
            }
            final Long _tmpNextReviewTime;
            if (_cursor.isNull(_cursorIndexOfNextReviewTime)) {
              _tmpNextReviewTime = null;
            } else {
              _tmpNextReviewTime = _cursor.getLong(_cursorIndexOfNextReviewTime);
            }
            final int _tmpReviewLevel;
            _tmpReviewLevel = _cursor.getInt(_cursorIndexOfReviewLevel);
            _item = new VocabularyWord(_tmpId,_tmpWord,_tmpReading,_tmpMeaning,_tmpExplanation,_tmpPartOfSpeech,_tmpCategory,_tmpSourceArticleTitle,_tmpSourceArticleUrl,_tmpAddedTime,_tmpIsManuallyAdded,_tmpIsFavorite,_tmpReviewCount,_tmpCorrectCount,_tmpLastReviewTime,_tmpNextReviewTime,_tmpReviewLevel);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getWordsForReview(final long currentTime,
      final Continuation<? super List<VocabularyWord>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM vocabulary\n"
            + "        WHERE nextReviewTime <= ?\n"
            + "        OR nextReviewTime IS NULL\n"
            + "        ORDER BY RANDOM()\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, currentTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<VocabularyWord>>() {
      @Override
      @NonNull
      public List<VocabularyWord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfReading = CursorUtil.getColumnIndexOrThrow(_cursor, "reading");
          final int _cursorIndexOfMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "meaning");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSourceArticleTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleTitle");
          final int _cursorIndexOfSourceArticleUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleUrl");
          final int _cursorIndexOfAddedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTime");
          final int _cursorIndexOfIsManuallyAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "isManuallyAdded");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final int _cursorIndexOfCorrectCount = CursorUtil.getColumnIndexOrThrow(_cursor, "correctCount");
          final int _cursorIndexOfLastReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReviewTime");
          final int _cursorIndexOfNextReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "nextReviewTime");
          final int _cursorIndexOfReviewLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewLevel");
          final List<VocabularyWord> _result = new ArrayList<VocabularyWord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VocabularyWord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpReading;
            if (_cursor.isNull(_cursorIndexOfReading)) {
              _tmpReading = null;
            } else {
              _tmpReading = _cursor.getString(_cursorIndexOfReading);
            }
            final String _tmpMeaning;
            _tmpMeaning = _cursor.getString(_cursorIndexOfMeaning);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpPartOfSpeech;
            if (_cursor.isNull(_cursorIndexOfPartOfSpeech)) {
              _tmpPartOfSpeech = null;
            } else {
              _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpSourceArticleTitle;
            if (_cursor.isNull(_cursorIndexOfSourceArticleTitle)) {
              _tmpSourceArticleTitle = null;
            } else {
              _tmpSourceArticleTitle = _cursor.getString(_cursorIndexOfSourceArticleTitle);
            }
            final String _tmpSourceArticleUrl;
            if (_cursor.isNull(_cursorIndexOfSourceArticleUrl)) {
              _tmpSourceArticleUrl = null;
            } else {
              _tmpSourceArticleUrl = _cursor.getString(_cursorIndexOfSourceArticleUrl);
            }
            final long _tmpAddedTime;
            _tmpAddedTime = _cursor.getLong(_cursorIndexOfAddedTime);
            final boolean _tmpIsManuallyAdded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManuallyAdded);
            _tmpIsManuallyAdded = _tmp != 0;
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            final int _tmpCorrectCount;
            _tmpCorrectCount = _cursor.getInt(_cursorIndexOfCorrectCount);
            final Long _tmpLastReviewTime;
            if (_cursor.isNull(_cursorIndexOfLastReviewTime)) {
              _tmpLastReviewTime = null;
            } else {
              _tmpLastReviewTime = _cursor.getLong(_cursorIndexOfLastReviewTime);
            }
            final Long _tmpNextReviewTime;
            if (_cursor.isNull(_cursorIndexOfNextReviewTime)) {
              _tmpNextReviewTime = null;
            } else {
              _tmpNextReviewTime = _cursor.getLong(_cursorIndexOfNextReviewTime);
            }
            final int _tmpReviewLevel;
            _tmpReviewLevel = _cursor.getInt(_cursorIndexOfReviewLevel);
            _item = new VocabularyWord(_tmpId,_tmpWord,_tmpReading,_tmpMeaning,_tmpExplanation,_tmpPartOfSpeech,_tmpCategory,_tmpSourceArticleTitle,_tmpSourceArticleUrl,_tmpAddedTime,_tmpIsManuallyAdded,_tmpIsFavorite,_tmpReviewCount,_tmpCorrectCount,_tmpLastReviewTime,_tmpNextReviewTime,_tmpReviewLevel);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Integer> getTotalCount() {
    final String _sql = "SELECT COUNT(*) FROM vocabulary";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vocabulary"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getTodayAddedCount(final long startOfDay) {
    final String _sql = "SELECT COUNT(*) FROM vocabulary WHERE addedTime >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startOfDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vocabulary"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getPendingReviewCount(final long currentTime) {
    final String _sql = "SELECT COUNT(*) FROM vocabulary WHERE nextReviewTime <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, currentTime);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vocabulary"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getMasteredCount() {
    final String _sql = "SELECT COUNT(*) FROM vocabulary WHERE reviewLevel >= 4";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vocabulary"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getWordsByTypeAndCategory(final String pos, final String cat, final long excludeId,
      final int limit, final Continuation<? super List<VocabularyWord>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM vocabulary\n"
            + "        WHERE partOfSpeech = ? AND category = ? AND id != ?\n"
            + "        ORDER BY RANDOM() LIMIT ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindString(_argIndex, pos);
    _argIndex = 2;
    _statement.bindString(_argIndex, cat);
    _argIndex = 3;
    _statement.bindLong(_argIndex, excludeId);
    _argIndex = 4;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<VocabularyWord>>() {
      @Override
      @NonNull
      public List<VocabularyWord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfReading = CursorUtil.getColumnIndexOrThrow(_cursor, "reading");
          final int _cursorIndexOfMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "meaning");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSourceArticleTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleTitle");
          final int _cursorIndexOfSourceArticleUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleUrl");
          final int _cursorIndexOfAddedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTime");
          final int _cursorIndexOfIsManuallyAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "isManuallyAdded");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final int _cursorIndexOfCorrectCount = CursorUtil.getColumnIndexOrThrow(_cursor, "correctCount");
          final int _cursorIndexOfLastReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReviewTime");
          final int _cursorIndexOfNextReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "nextReviewTime");
          final int _cursorIndexOfReviewLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewLevel");
          final List<VocabularyWord> _result = new ArrayList<VocabularyWord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VocabularyWord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpReading;
            if (_cursor.isNull(_cursorIndexOfReading)) {
              _tmpReading = null;
            } else {
              _tmpReading = _cursor.getString(_cursorIndexOfReading);
            }
            final String _tmpMeaning;
            _tmpMeaning = _cursor.getString(_cursorIndexOfMeaning);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpPartOfSpeech;
            if (_cursor.isNull(_cursorIndexOfPartOfSpeech)) {
              _tmpPartOfSpeech = null;
            } else {
              _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpSourceArticleTitle;
            if (_cursor.isNull(_cursorIndexOfSourceArticleTitle)) {
              _tmpSourceArticleTitle = null;
            } else {
              _tmpSourceArticleTitle = _cursor.getString(_cursorIndexOfSourceArticleTitle);
            }
            final String _tmpSourceArticleUrl;
            if (_cursor.isNull(_cursorIndexOfSourceArticleUrl)) {
              _tmpSourceArticleUrl = null;
            } else {
              _tmpSourceArticleUrl = _cursor.getString(_cursorIndexOfSourceArticleUrl);
            }
            final long _tmpAddedTime;
            _tmpAddedTime = _cursor.getLong(_cursorIndexOfAddedTime);
            final boolean _tmpIsManuallyAdded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManuallyAdded);
            _tmpIsManuallyAdded = _tmp != 0;
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            final int _tmpCorrectCount;
            _tmpCorrectCount = _cursor.getInt(_cursorIndexOfCorrectCount);
            final Long _tmpLastReviewTime;
            if (_cursor.isNull(_cursorIndexOfLastReviewTime)) {
              _tmpLastReviewTime = null;
            } else {
              _tmpLastReviewTime = _cursor.getLong(_cursorIndexOfLastReviewTime);
            }
            final Long _tmpNextReviewTime;
            if (_cursor.isNull(_cursorIndexOfNextReviewTime)) {
              _tmpNextReviewTime = null;
            } else {
              _tmpNextReviewTime = _cursor.getLong(_cursorIndexOfNextReviewTime);
            }
            final int _tmpReviewLevel;
            _tmpReviewLevel = _cursor.getInt(_cursorIndexOfReviewLevel);
            _item = new VocabularyWord(_tmpId,_tmpWord,_tmpReading,_tmpMeaning,_tmpExplanation,_tmpPartOfSpeech,_tmpCategory,_tmpSourceArticleTitle,_tmpSourceArticleUrl,_tmpAddedTime,_tmpIsManuallyAdded,_tmpIsFavorite,_tmpReviewCount,_tmpCorrectCount,_tmpLastReviewTime,_tmpNextReviewTime,_tmpReviewLevel);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getWordsByType(final String pos, final List<Long> excludeIds, final int limit,
      final Continuation<? super List<VocabularyWord>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("\n");
    _stringBuilder.append("        SELECT * FROM vocabulary");
    _stringBuilder.append("\n");
    _stringBuilder.append("        WHERE partOfSpeech = ");
    _stringBuilder.append("?");
    _stringBuilder.append(" AND id NOT IN (");
    final int _inputSize = excludeIds.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    _stringBuilder.append("\n");
    _stringBuilder.append("        ORDER BY RANDOM() LIMIT ");
    _stringBuilder.append("?");
    _stringBuilder.append("\n");
    _stringBuilder.append("    ");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 2 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    _statement.bindString(_argIndex, pos);
    _argIndex = 2;
    for (long _item : excludeIds) {
      _statement.bindLong(_argIndex, _item);
      _argIndex++;
    }
    _argIndex = 2 + _inputSize;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<VocabularyWord>>() {
      @Override
      @NonNull
      public List<VocabularyWord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfReading = CursorUtil.getColumnIndexOrThrow(_cursor, "reading");
          final int _cursorIndexOfMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "meaning");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSourceArticleTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleTitle");
          final int _cursorIndexOfSourceArticleUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleUrl");
          final int _cursorIndexOfAddedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTime");
          final int _cursorIndexOfIsManuallyAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "isManuallyAdded");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final int _cursorIndexOfCorrectCount = CursorUtil.getColumnIndexOrThrow(_cursor, "correctCount");
          final int _cursorIndexOfLastReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReviewTime");
          final int _cursorIndexOfNextReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "nextReviewTime");
          final int _cursorIndexOfReviewLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewLevel");
          final List<VocabularyWord> _result = new ArrayList<VocabularyWord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VocabularyWord _item_1;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpReading;
            if (_cursor.isNull(_cursorIndexOfReading)) {
              _tmpReading = null;
            } else {
              _tmpReading = _cursor.getString(_cursorIndexOfReading);
            }
            final String _tmpMeaning;
            _tmpMeaning = _cursor.getString(_cursorIndexOfMeaning);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpPartOfSpeech;
            if (_cursor.isNull(_cursorIndexOfPartOfSpeech)) {
              _tmpPartOfSpeech = null;
            } else {
              _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpSourceArticleTitle;
            if (_cursor.isNull(_cursorIndexOfSourceArticleTitle)) {
              _tmpSourceArticleTitle = null;
            } else {
              _tmpSourceArticleTitle = _cursor.getString(_cursorIndexOfSourceArticleTitle);
            }
            final String _tmpSourceArticleUrl;
            if (_cursor.isNull(_cursorIndexOfSourceArticleUrl)) {
              _tmpSourceArticleUrl = null;
            } else {
              _tmpSourceArticleUrl = _cursor.getString(_cursorIndexOfSourceArticleUrl);
            }
            final long _tmpAddedTime;
            _tmpAddedTime = _cursor.getLong(_cursorIndexOfAddedTime);
            final boolean _tmpIsManuallyAdded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManuallyAdded);
            _tmpIsManuallyAdded = _tmp != 0;
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            final int _tmpCorrectCount;
            _tmpCorrectCount = _cursor.getInt(_cursorIndexOfCorrectCount);
            final Long _tmpLastReviewTime;
            if (_cursor.isNull(_cursorIndexOfLastReviewTime)) {
              _tmpLastReviewTime = null;
            } else {
              _tmpLastReviewTime = _cursor.getLong(_cursorIndexOfLastReviewTime);
            }
            final Long _tmpNextReviewTime;
            if (_cursor.isNull(_cursorIndexOfNextReviewTime)) {
              _tmpNextReviewTime = null;
            } else {
              _tmpNextReviewTime = _cursor.getLong(_cursorIndexOfNextReviewTime);
            }
            final int _tmpReviewLevel;
            _tmpReviewLevel = _cursor.getInt(_cursorIndexOfReviewLevel);
            _item_1 = new VocabularyWord(_tmpId,_tmpWord,_tmpReading,_tmpMeaning,_tmpExplanation,_tmpPartOfSpeech,_tmpCategory,_tmpSourceArticleTitle,_tmpSourceArticleUrl,_tmpAddedTime,_tmpIsManuallyAdded,_tmpIsFavorite,_tmpReviewCount,_tmpCorrectCount,_tmpLastReviewTime,_tmpNextReviewTime,_tmpReviewLevel);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getWordsBySimilarLength(final int minLen, final int maxLen,
      final List<Long> excludeIds, final int limit,
      final Continuation<? super List<VocabularyWord>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("\n");
    _stringBuilder.append("        SELECT * FROM vocabulary");
    _stringBuilder.append("\n");
    _stringBuilder.append("        WHERE LENGTH(word) BETWEEN ");
    _stringBuilder.append("?");
    _stringBuilder.append(" AND ");
    _stringBuilder.append("?");
    _stringBuilder.append("\n");
    _stringBuilder.append("        AND id NOT IN (");
    final int _inputSize = excludeIds.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    _stringBuilder.append("\n");
    _stringBuilder.append("        ORDER BY RANDOM() LIMIT ");
    _stringBuilder.append("?");
    _stringBuilder.append("\n");
    _stringBuilder.append("    ");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 3 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, minLen);
    _argIndex = 2;
    _statement.bindLong(_argIndex, maxLen);
    _argIndex = 3;
    for (long _item : excludeIds) {
      _statement.bindLong(_argIndex, _item);
      _argIndex++;
    }
    _argIndex = 3 + _inputSize;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<VocabularyWord>>() {
      @Override
      @NonNull
      public List<VocabularyWord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfReading = CursorUtil.getColumnIndexOrThrow(_cursor, "reading");
          final int _cursorIndexOfMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "meaning");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSourceArticleTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleTitle");
          final int _cursorIndexOfSourceArticleUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleUrl");
          final int _cursorIndexOfAddedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTime");
          final int _cursorIndexOfIsManuallyAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "isManuallyAdded");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final int _cursorIndexOfCorrectCount = CursorUtil.getColumnIndexOrThrow(_cursor, "correctCount");
          final int _cursorIndexOfLastReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReviewTime");
          final int _cursorIndexOfNextReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "nextReviewTime");
          final int _cursorIndexOfReviewLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewLevel");
          final List<VocabularyWord> _result = new ArrayList<VocabularyWord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VocabularyWord _item_1;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpReading;
            if (_cursor.isNull(_cursorIndexOfReading)) {
              _tmpReading = null;
            } else {
              _tmpReading = _cursor.getString(_cursorIndexOfReading);
            }
            final String _tmpMeaning;
            _tmpMeaning = _cursor.getString(_cursorIndexOfMeaning);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpPartOfSpeech;
            if (_cursor.isNull(_cursorIndexOfPartOfSpeech)) {
              _tmpPartOfSpeech = null;
            } else {
              _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpSourceArticleTitle;
            if (_cursor.isNull(_cursorIndexOfSourceArticleTitle)) {
              _tmpSourceArticleTitle = null;
            } else {
              _tmpSourceArticleTitle = _cursor.getString(_cursorIndexOfSourceArticleTitle);
            }
            final String _tmpSourceArticleUrl;
            if (_cursor.isNull(_cursorIndexOfSourceArticleUrl)) {
              _tmpSourceArticleUrl = null;
            } else {
              _tmpSourceArticleUrl = _cursor.getString(_cursorIndexOfSourceArticleUrl);
            }
            final long _tmpAddedTime;
            _tmpAddedTime = _cursor.getLong(_cursorIndexOfAddedTime);
            final boolean _tmpIsManuallyAdded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManuallyAdded);
            _tmpIsManuallyAdded = _tmp != 0;
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            final int _tmpCorrectCount;
            _tmpCorrectCount = _cursor.getInt(_cursorIndexOfCorrectCount);
            final Long _tmpLastReviewTime;
            if (_cursor.isNull(_cursorIndexOfLastReviewTime)) {
              _tmpLastReviewTime = null;
            } else {
              _tmpLastReviewTime = _cursor.getLong(_cursorIndexOfLastReviewTime);
            }
            final Long _tmpNextReviewTime;
            if (_cursor.isNull(_cursorIndexOfNextReviewTime)) {
              _tmpNextReviewTime = null;
            } else {
              _tmpNextReviewTime = _cursor.getLong(_cursorIndexOfNextReviewTime);
            }
            final int _tmpReviewLevel;
            _tmpReviewLevel = _cursor.getInt(_cursorIndexOfReviewLevel);
            _item_1 = new VocabularyWord(_tmpId,_tmpWord,_tmpReading,_tmpMeaning,_tmpExplanation,_tmpPartOfSpeech,_tmpCategory,_tmpSourceArticleTitle,_tmpSourceArticleUrl,_tmpAddedTime,_tmpIsManuallyAdded,_tmpIsFavorite,_tmpReviewCount,_tmpCorrectCount,_tmpLastReviewTime,_tmpNextReviewTime,_tmpReviewLevel);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRandomWords(final int limit, final List<Long> excludeIds,
      final Continuation<? super List<VocabularyWord>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("\n");
    _stringBuilder.append("        SELECT * FROM vocabulary");
    _stringBuilder.append("\n");
    _stringBuilder.append("        WHERE id NOT IN (");
    final int _inputSize = excludeIds.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    _stringBuilder.append("\n");
    _stringBuilder.append("        ORDER BY RANDOM() LIMIT ");
    _stringBuilder.append("?");
    _stringBuilder.append("\n");
    _stringBuilder.append("    ");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 1 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (long _item : excludeIds) {
      _statement.bindLong(_argIndex, _item);
      _argIndex++;
    }
    _argIndex = 1 + _inputSize;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<VocabularyWord>>() {
      @Override
      @NonNull
      public List<VocabularyWord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfReading = CursorUtil.getColumnIndexOrThrow(_cursor, "reading");
          final int _cursorIndexOfMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "meaning");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSourceArticleTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleTitle");
          final int _cursorIndexOfSourceArticleUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleUrl");
          final int _cursorIndexOfAddedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTime");
          final int _cursorIndexOfIsManuallyAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "isManuallyAdded");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final int _cursorIndexOfCorrectCount = CursorUtil.getColumnIndexOrThrow(_cursor, "correctCount");
          final int _cursorIndexOfLastReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReviewTime");
          final int _cursorIndexOfNextReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "nextReviewTime");
          final int _cursorIndexOfReviewLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewLevel");
          final List<VocabularyWord> _result = new ArrayList<VocabularyWord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VocabularyWord _item_1;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpReading;
            if (_cursor.isNull(_cursorIndexOfReading)) {
              _tmpReading = null;
            } else {
              _tmpReading = _cursor.getString(_cursorIndexOfReading);
            }
            final String _tmpMeaning;
            _tmpMeaning = _cursor.getString(_cursorIndexOfMeaning);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpPartOfSpeech;
            if (_cursor.isNull(_cursorIndexOfPartOfSpeech)) {
              _tmpPartOfSpeech = null;
            } else {
              _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpSourceArticleTitle;
            if (_cursor.isNull(_cursorIndexOfSourceArticleTitle)) {
              _tmpSourceArticleTitle = null;
            } else {
              _tmpSourceArticleTitle = _cursor.getString(_cursorIndexOfSourceArticleTitle);
            }
            final String _tmpSourceArticleUrl;
            if (_cursor.isNull(_cursorIndexOfSourceArticleUrl)) {
              _tmpSourceArticleUrl = null;
            } else {
              _tmpSourceArticleUrl = _cursor.getString(_cursorIndexOfSourceArticleUrl);
            }
            final long _tmpAddedTime;
            _tmpAddedTime = _cursor.getLong(_cursorIndexOfAddedTime);
            final boolean _tmpIsManuallyAdded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManuallyAdded);
            _tmpIsManuallyAdded = _tmp != 0;
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            final int _tmpCorrectCount;
            _tmpCorrectCount = _cursor.getInt(_cursorIndexOfCorrectCount);
            final Long _tmpLastReviewTime;
            if (_cursor.isNull(_cursorIndexOfLastReviewTime)) {
              _tmpLastReviewTime = null;
            } else {
              _tmpLastReviewTime = _cursor.getLong(_cursorIndexOfLastReviewTime);
            }
            final Long _tmpNextReviewTime;
            if (_cursor.isNull(_cursorIndexOfNextReviewTime)) {
              _tmpNextReviewTime = null;
            } else {
              _tmpNextReviewTime = _cursor.getLong(_cursorIndexOfNextReviewTime);
            }
            final int _tmpReviewLevel;
            _tmpReviewLevel = _cursor.getInt(_cursorIndexOfReviewLevel);
            _item_1 = new VocabularyWord(_tmpId,_tmpWord,_tmpReading,_tmpMeaning,_tmpExplanation,_tmpPartOfSpeech,_tmpCategory,_tmpSourceArticleTitle,_tmpSourceArticleUrl,_tmpAddedTime,_tmpIsManuallyAdded,_tmpIsFavorite,_tmpReviewCount,_tmpCorrectCount,_tmpLastReviewTime,_tmpNextReviewTime,_tmpReviewLevel);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTagsForWord(final long wordId,
      final Continuation<? super List<WordTag>> $completion) {
    final String _sql = "SELECT * FROM word_tags WHERE wordId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, wordId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<WordTag>>() {
      @Override
      @NonNull
      public List<WordTag> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWordId = CursorUtil.getColumnIndexOrThrow(_cursor, "wordId");
          final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
          final List<WordTag> _result = new ArrayList<WordTag>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordTag _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpWordId;
            _tmpWordId = _cursor.getLong(_cursorIndexOfWordId);
            final String _tmpTag;
            _tmpTag = _cursor.getString(_cursorIndexOfTag);
            _item = new WordTag(_tmpId,_tmpWordId,_tmpTag);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<VocabularyWord>> getWordsByTags(final List<String> tags) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("\n");
    _stringBuilder.append("        SELECT v.* FROM vocabulary v");
    _stringBuilder.append("\n");
    _stringBuilder.append("        INNER JOIN word_tags t ON v.id = t.wordId");
    _stringBuilder.append("\n");
    _stringBuilder.append("        WHERE t.tag IN (");
    final int _inputSize = tags.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    _stringBuilder.append("\n");
    _stringBuilder.append("        GROUP BY v.id");
    _stringBuilder.append("\n");
    _stringBuilder.append("        ORDER BY v.addedTime DESC");
    _stringBuilder.append("\n");
    _stringBuilder.append("    ");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : tags) {
      _statement.bindString(_argIndex, _item);
      _argIndex++;
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vocabulary",
        "word_tags"}, new Callable<List<VocabularyWord>>() {
      @Override
      @NonNull
      public List<VocabularyWord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfReading = CursorUtil.getColumnIndexOrThrow(_cursor, "reading");
          final int _cursorIndexOfMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "meaning");
          final int _cursorIndexOfExplanation = CursorUtil.getColumnIndexOrThrow(_cursor, "explanation");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfSourceArticleTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleTitle");
          final int _cursorIndexOfSourceArticleUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceArticleUrl");
          final int _cursorIndexOfAddedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "addedTime");
          final int _cursorIndexOfIsManuallyAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "isManuallyAdded");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final int _cursorIndexOfCorrectCount = CursorUtil.getColumnIndexOrThrow(_cursor, "correctCount");
          final int _cursorIndexOfLastReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReviewTime");
          final int _cursorIndexOfNextReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "nextReviewTime");
          final int _cursorIndexOfReviewLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewLevel");
          final List<VocabularyWord> _result = new ArrayList<VocabularyWord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VocabularyWord _item_1;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpReading;
            if (_cursor.isNull(_cursorIndexOfReading)) {
              _tmpReading = null;
            } else {
              _tmpReading = _cursor.getString(_cursorIndexOfReading);
            }
            final String _tmpMeaning;
            _tmpMeaning = _cursor.getString(_cursorIndexOfMeaning);
            final String _tmpExplanation;
            _tmpExplanation = _cursor.getString(_cursorIndexOfExplanation);
            final String _tmpPartOfSpeech;
            if (_cursor.isNull(_cursorIndexOfPartOfSpeech)) {
              _tmpPartOfSpeech = null;
            } else {
              _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpSourceArticleTitle;
            if (_cursor.isNull(_cursorIndexOfSourceArticleTitle)) {
              _tmpSourceArticleTitle = null;
            } else {
              _tmpSourceArticleTitle = _cursor.getString(_cursorIndexOfSourceArticleTitle);
            }
            final String _tmpSourceArticleUrl;
            if (_cursor.isNull(_cursorIndexOfSourceArticleUrl)) {
              _tmpSourceArticleUrl = null;
            } else {
              _tmpSourceArticleUrl = _cursor.getString(_cursorIndexOfSourceArticleUrl);
            }
            final long _tmpAddedTime;
            _tmpAddedTime = _cursor.getLong(_cursorIndexOfAddedTime);
            final boolean _tmpIsManuallyAdded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManuallyAdded);
            _tmpIsManuallyAdded = _tmp != 0;
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            final int _tmpCorrectCount;
            _tmpCorrectCount = _cursor.getInt(_cursorIndexOfCorrectCount);
            final Long _tmpLastReviewTime;
            if (_cursor.isNull(_cursorIndexOfLastReviewTime)) {
              _tmpLastReviewTime = null;
            } else {
              _tmpLastReviewTime = _cursor.getLong(_cursorIndexOfLastReviewTime);
            }
            final Long _tmpNextReviewTime;
            if (_cursor.isNull(_cursorIndexOfNextReviewTime)) {
              _tmpNextReviewTime = null;
            } else {
              _tmpNextReviewTime = _cursor.getLong(_cursorIndexOfNextReviewTime);
            }
            final int _tmpReviewLevel;
            _tmpReviewLevel = _cursor.getInt(_cursorIndexOfReviewLevel);
            _item_1 = new VocabularyWord(_tmpId,_tmpWord,_tmpReading,_tmpMeaning,_tmpExplanation,_tmpPartOfSpeech,_tmpCategory,_tmpSourceArticleTitle,_tmpSourceArticleUrl,_tmpAddedTime,_tmpIsManuallyAdded,_tmpIsFavorite,_tmpReviewCount,_tmpCorrectCount,_tmpLastReviewTime,_tmpNextReviewTime,_tmpReviewLevel);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getCachedQuestion(final long wordId,
      final Continuation<? super CachedQuestion> $completion) {
    final String _sql = "SELECT * FROM cached_questions WHERE wordId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, wordId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CachedQuestion>() {
      @Override
      @Nullable
      public CachedQuestion call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfWordId = CursorUtil.getColumnIndexOrThrow(_cursor, "wordId");
          final int _cursorIndexOfDistractors = CursorUtil.getColumnIndexOrThrow(_cursor, "distractors");
          final int _cursorIndexOfGeneratedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "generatedTime");
          final int _cursorIndexOfExpiresAt = CursorUtil.getColumnIndexOrThrow(_cursor, "expiresAt");
          final CachedQuestion _result;
          if (_cursor.moveToFirst()) {
            final long _tmpWordId;
            _tmpWordId = _cursor.getLong(_cursorIndexOfWordId);
            final List<String> _tmpDistractors;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDistractors);
            _tmpDistractors = __stringListConverter.fromString(_tmp);
            final long _tmpGeneratedTime;
            _tmpGeneratedTime = _cursor.getLong(_cursorIndexOfGeneratedTime);
            final long _tmpExpiresAt;
            _tmpExpiresAt = _cursor.getLong(_cursorIndexOfExpiresAt);
            _result = new CachedQuestion(_tmpWordId,_tmpDistractors,_tmpGeneratedTime,_tmpExpiresAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getReviewHistory(final long wordId,
      final Continuation<? super List<ReviewSession>> $completion) {
    final String _sql = "SELECT * FROM review_sessions WHERE wordId = ? ORDER BY reviewTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, wordId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReviewSession>>() {
      @Override
      @NonNull
      public List<ReviewSession> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWordId = CursorUtil.getColumnIndexOrThrow(_cursor, "wordId");
          final int _cursorIndexOfReviewTime = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewTime");
          final int _cursorIndexOfIsCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "isCorrect");
          final List<ReviewSession> _result = new ArrayList<ReviewSession>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReviewSession _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpWordId;
            _tmpWordId = _cursor.getLong(_cursorIndexOfWordId);
            final long _tmpReviewTime;
            _tmpReviewTime = _cursor.getLong(_cursorIndexOfReviewTime);
            final boolean _tmpIsCorrect;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCorrect);
            _tmpIsCorrect = _tmp != 0;
            _item = new ReviewSession(_tmpId,_tmpWordId,_tmpReviewTime,_tmpIsCorrect);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteWords(final List<Long> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM vocabulary WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (long _item : ids) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
