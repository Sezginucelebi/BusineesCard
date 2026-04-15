import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../models/business_card.dart';

class DatabaseService {
  static final DatabaseService _instance = DatabaseService._internal();
  static Database? _database;

  factory DatabaseService() => _instance;

  DatabaseService._internal();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    String path = join(await getDatabasesPath(), 'businees_card_database.db');
    return await openDatabase(
      path,
      version: 1,
      onCreate: (db, version) {
        return db.execute(
          'CREATE TABLE cards(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, title TEXT, company TEXT, address TEXT, phones TEXT, email TEXT, website TEXT, photoPath TEXT, cardColor TEXT, fontStyle TEXT, userId TEXT)',
        );
      },
    );
  }

  Future<int> insertCard(BusinessCard card) async {
    final db = await database;
    return await db.insert('cards', card.toMap(), conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<List<BusinessCard>> getCards(String userId) async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'cards',
      where: 'userId = ?',
      whereArgs: [userId],
    );
    return List.generate(maps.length, (i) {
      return BusinessCard.fromMap(maps[i]);
    });
  }

  Future<int> updateCard(BusinessCard card) async {
    final db = await database;
    return await db.update(
      'cards',
      card.toMap(),
      where: 'id = ?',
      whereArgs: [card.id],
    );
  }

  Future<int> deleteCard(int id) async {
    final db = await database;
    return await db.delete(
      'cards',
      where: 'id = ?',
      whereArgs: [id],
    );
  }
}
