import SwiftUI

struct CardListView: View {
    @EnvironmentObject var authService: AuthService
    @State private var cards: [BusinessCard] = []
    @State private var isLoading = true
    @State private var showingEditor = false
    @State private var showingStore = false
    @State private var selectedCard: BusinessCard? = nil
    @State private var cardToEdit: BusinessCard? = nil

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                HStack {
                    Text("Paket: \(authService.getSubscriptionLevel().rawValue)")
                        .fontWeight(.bold)
                    Spacer()
                    Text("Kart: \(cards.count) / \(authService.getCardLimit())")
                }
                .font(.caption)
                .padding(10)
                .background(Color(white: 0.95))

                if isLoading {
                    ProgressView().frame(maxHeight: .infinity)
                } else if cards.isEmpty {
                    VStack {
                        Text("Henuz bir kart eklemediniz.")
                            .foregroundColor(.gray)
                    }
                    .frame(maxHeight: .infinity)
                } else {
                    List {
                        ForEach(cards) { card in
                            CardRow(card: card)
                                .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                                .listRowSeparator(.hidden)
                                .onTapGesture {
                                    selectedCard = card
                                }
                                .swipeActions(edge: .trailing) {
                                    Button(role: .destructive) {
                                        deleteCard(card: card)
                                    } label: {
                                        Label("Sil", systemImage: "trash")
                                    }

                                    Button {
                                        cardToEdit = card
                                    } label: {
                                        Label("Duzenle", systemImage: "pencil")
                                    }
                                    .tint(.blue)
                                }
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Kartvizitlerim")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    HStack {
                        Button(action: { showingStore = true }) {
                            Image(systemName: "cart.fill").foregroundColor(.orange)
                        }
                        Button(action: handleSignOut) {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                        }
                    }
                }
                ToolbarItem(placement: .bottomBar) {
                    Button(action: { showingEditor = true }) {
                        HStack {
                            Image(systemName: "plus.circle.fill")
                            Text("Yeni Kart Ekle")
                        }
                        .bold()
                    }
                }
            }
            .sheet(isPresented: $showingEditor, onDismiss: loadCards) {
                CardEditorView().environmentObject(authService)
            }
            .sheet(isPresented: $showingStore) {
                StoreView().environmentObject(authService)
            }
            .sheet(item: $cardToEdit, onDismiss: loadCards) { card in
                CardEditorView(card: card).environmentObject(authService)
            }
            .background(
                NavigationLink(
                    destination: detailDestination,
                    isActive: selectedCardBinding
                ) {
                    EmptyView()
                }
                .hidden()
            )
            .onAppear(perform: loadCards)
        }
    }

    private var detailDestination: some View {
        Group {
            if let selectedCard {
                CardDetailsView(card: selectedCard)
                    .environmentObject(authService)
            } else {
                EmptyView()
            }
        }
    }

    private var selectedCardBinding: Binding<Bool> {
        Binding(
            get: { selectedCard != nil },
            set: { if !$0 { selectedCard = nil } }
        )
    }

    func loadCards() {
        Task {
            let userId = authService.currentUser?.uid ?? ""
            cards = (try? await DatabaseService.shared.getCards(userId: userId)) ?? []
            isLoading = false
        }
    }

    func deleteCard(card: BusinessCard) {
        Task {
            let userId = authService.currentUser?.uid ?? ""
            if let id = card.id {
                try? await DatabaseService.shared.deleteCard(userId: userId, cardId: id)
                loadCards()
            }
        }
    }

    func handleSignOut() {
        do {
            try authService.signOut()
        } catch {
            print("Cikis hatasi: \(error.localizedDescription)")
        }
    }
}

struct CardRow: View {
    let card: BusinessCard

    var body: some View {
        HStack(spacing: 15) {
            Circle()
                .fill(Color(hex: card.cardColor.replacingOccurrences(of: "0xFF", with: "")))
                .frame(width: 45, height: 45)
                .overlay(
                    Text(card.name.prefix(1).uppercased())
                        .foregroundColor(card.cardColor == "0xFF2C2C2C" ? .white : .black)
                        .bold()
                )

            VStack(alignment: .leading, spacing: 4) {
                Text(card.name)
                    .font(.headline)
                Text("\(card.title) @ \(card.company)")
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundColor(.gray)
        }
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}
