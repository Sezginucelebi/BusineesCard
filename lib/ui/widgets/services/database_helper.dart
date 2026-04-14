import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../models/business_card.dart';

class DatabaseHelper {
  static final DatabaseHelper _instance = DatabaseHelper._internal();
  static Database? _database;

  factory DatabaseHelper() => _instance;

  DatabaseHelper._internal();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    String path = join(await getDatabasesPath(), 'business_cards.db');
    return await openDatabase(
      path,
      version: 1,
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE cards (
            id TEXT PRIMARY KEY,
            name TEXT,
            title TEXT,
            company TEXT,
            address TEXT,
            phones TEXT,
            email TEXT,
            website TEXT,
            photoUri TEXT,
            cardColor INTEGER,
            fontStyle TEXT,
            userId TEXT
          )
        ''');
      },
    );
  }

  Future<void> insertCard(BusinessCard card) async {
    final db = await database;
    await db.insert('cards', card.toMap(), conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<List<BusinessCard>> getCards(String userId) async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query('cards', where: 'userId = ?', whereArgs: [userId]);
    return List.generate(maps.length, (i) => BusinessCard.fromMap(maps[i]));
  }

  Future<void> deleteCard(String id) async {
    final db = await database;
    await db.delete('cards', where: 'id = ?', whereArgs: [id]);
  }
}