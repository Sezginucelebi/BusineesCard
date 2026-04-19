import SwiftUI

struct CardListView: View {
    @StateObject private var viewModel = CardListViewModel()
    @EnvironmentObject var authService: AuthService
    @State private var showingEditor = false
    @State private var showingStore = false

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Subscription Info Bar
                HStack {
                    Text("Paket: \(authService.getSubscriptionLevel().rawValue.uppercased())")
                        .font(.caption).bold()
                    Spacer()
                    Text("Kart: \(viewModel.cards.count) / \(authService.getCardLimit())")
                        .font(.caption)
                }
                .padding(.horizontal)
                .frame(height: 30)
                .background(Color(.systemGray6))

                if viewModel.isLoading {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if viewModel.cards.isEmpty {
                    emptyState
                } else {
                    List {
                        ForEach(viewModel.cards) { card in
                            NavigationLink(destination: CardDetailsView(card: card)) {
                                BusinessCardRow(card: card)
                            }
                            .swipeActions {
                                Button(role: .destructive) {
                                    Task { await viewModel.deleteCard(card) }
                                } label: {
                                    Label("Sil", systemImage: "trash")
                                }
                            }
                        }
                    }
                    .listStyle(PlainListStyle())
                }
            }
            .navigationTitle("Kartvizitlerim")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { showingStore = true }) {
                        Image(systemName: "cart.fill").foregroundColor(.orange)
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { try? authService.signOut() }) {
                        Image(systemName: "rectangle.portrait.and.arrow.right")
                    }
                }
            }
            .overlay(alignment: .bottomTrailing) {
                Button(action: {
                    if viewModel.cards.count < authService.getCardLimit() {
                        showingEditor = true
                    } else {
                        // Limit exceeded logic - maybe show store
                        showingStore = true
                    }
                }) {
                    Image(systemName: "plus")
                        .font(.title.bold())
                        .foregroundColor(.white)
                        .padding()
                        .background(Color.blueGrey)
                        .clipShape(Circle())
                        .shadow(radius: 4)
                }
                .padding(24)
            }
            .sheet(isPresented: $showingEditor) {
                CardEditorView()
            }
            .sheet(isPresented: $showingStore) {
                StoreView()
            }
            .onAppear {
                viewModel.loadCards(userId: authService.currentUser?.uid ?? "")
            }
        }
    }

    var emptyState: some View {
        VStack(spacing: 20) {
            Spacer()
            Image(systemName: "person.crop.rectangle.stack")
                .font(.system(size: 80))
                .foregroundColor(.gray)
            Text("Henüz bir kart eklemediniz.")
                .foregroundColor(.gray)
            Button("İlk Kartını Oluştur") { showingEditor = true }
                .buttonStyle(.borderedProminent)
                .tint(.blueGrey)
            Spacer()
        }
    }
}

class CardListViewModel: ObservableObject {
    @Published var cards: [BusinessCard] = []
    @Published var isLoading = false

    func loadCards(userId: String) {
        isLoading = true
        Task {
            do {
                let fetchedCards = try await DatabaseService.shared.getCards(for: userId)
                DispatchQueue.main.async {
                    self.cards = fetchedCards
                    self.isLoading = false
                }
            } catch {
                DispatchQueue.main.async { self.isLoading = false }
            }
        }
    }

    func deleteCard(_ card: BusinessCard) async {
        try? await DatabaseService.shared.deleteCard(id: card.id ?? "", userId: card.userId)
        loadCards(userId: card.userId)
    }
}

struct BusinessCardRow: View {
    let card: BusinessCard

    var body: some View {
        HStack(spacing: 15) {
            Circle()
                .fill(Color(hex: card.cardColor.replacingOccurrences(of: "0xFF", with: "")))
                .frame(width: 50, height: 50)
                .overlay(Text(card.name.prefix(1).uppercased()).bold())

            VStack(alignment: .leading) {
                Text(card.name).font(.headline)
                Text("\(card.title) - \(card.company)").font(.subheadline).foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 4)
    }
}
