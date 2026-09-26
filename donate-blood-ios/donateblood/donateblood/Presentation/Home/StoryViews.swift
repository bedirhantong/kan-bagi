import SwiftUI

private let storyRing = Color(red: 1.0, green: 0.514, blue: 0.263) // turuncu halka

/// Ana sayfa üstündeki yatay story şeridi.
struct StoryStrip: View {
    let stories: [Story]
    let onOpen: (Story) -> Void

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 14) {
                ForEach(stories) { story in
                    Button { onOpen(story) } label: {
                        VStack(spacing: 6) {
                            StoryAvatar(story: story, size: 72)
                                .padding(3)
                                .overlay(Circle().stroke(storyRing, lineWidth: 3))
                            Text(story.title).font(.caption).lineLimit(1).frame(width: 80)
                        }
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(Text(story.title))
                }
            }
            .padding(.horizontal)
        }
    }
}

struct StoryAvatar: View {
    let story: Story
    let size: CGFloat

    var body: some View {
        Group {
            if let url = story.logoURL ?? story.imageURL {
                RemoteImage(url: url, pointSize: size) { initialView }
            } else {
                initialView
            }
        }
        .frame(width: size, height: size)
        .clipShape(Circle())
    }

    private var initialView: some View {
        ZStack {
            Theme.brand.opacity(0.12)
            Text(story.initial).font(.system(size: size * 0.4, weight: .bold)).foregroundStyle(Theme.brand)
        }
    }
}

/// Tam ekran story görüntüleyici: üstte ilerleme çubukları, otomatik geçiş, kapat ve "Daha Fazla Bilgi".
struct StoryViewer: View {
    let stories: [Story]
    let start: Story

    @Environment(\.dismiss) private var dismiss
    @Environment(\.openURL) private var openURL
    @State private var index = 0
    @State private var progress: CGFloat = 0

    private let duration: TimeInterval = 6
    private let timer = Timer.publish(every: 0.05, on: .main, in: .common).autoconnect()

    private var story: Story { stories[min(index, stories.count - 1)] }

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            VStack(spacing: 12) {
                progressBars
                header
                Spacer(minLength: 0)
                content
                Spacer(minLength: 0)
                if let link = story.linkURL {
                    Button { openURL(link) } label: {
                        Label("story.moreInfo", systemImage: "chevron.up").font(.headline).foregroundStyle(.white)
                    }
                    .padding(.bottom, 24)
                }
            }
            .padding(.horizontal)
            HStack(spacing: 0) {
                Color.clear.contentShape(Rectangle()).onTapGesture { previous() }
                Color.clear.contentShape(Rectangle()).onTapGesture { next() }
            }
            .padding(.top, 120)
        }
        .onAppear { index = stories.firstIndex(of: start) ?? 0 }
        .onReceive(timer) { _ in
            progress += CGFloat(0.05 / duration)
            if progress >= 1 { next() }
        }
    }

    private var progressBars: some View {
        HStack(spacing: 4) {
            ForEach(stories.indices, id: \.self) { i in
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        Capsule().fill(Color.white.opacity(0.3))
                        Capsule().fill(Color.white).frame(width: geo.size.width * (i < index ? 1 : i == index ? progress : 0))
                    }
                }
                .frame(height: 3)
            }
        }
        .accessibilityHidden(true)
    }

    private var header: some View {
        HStack(spacing: 10) {
            StoryAvatar(story: story, size: 36)
            Text(story.title).font(.headline).foregroundStyle(.white)
            Spacer()
            Button { dismiss() } label: { Image(systemName: "xmark").font(.title3).foregroundStyle(.white) }
                .accessibilityLabel(Text("common.close"))
        }
    }

    @ViewBuilder
    private var content: some View {
        VStack(spacing: 20) {
            if let url = story.imageURL {
                RemoteImage(url: url, pointSize: 600, contentMode: .fit) { ProgressView().tint(.white) }
                    .frame(maxHeight: 420)
            }
            if let body = story.body {
                Text(body).font(.title3.weight(.medium)).foregroundStyle(.white).multilineTextAlignment(.center)
            }
        }
    }

    private func next() {
        progress = 0
        if index < stories.count - 1 { index += 1 } else { dismiss() }
    }

    private func previous() {
        progress = 0
        if index > 0 { index -= 1 }
    }
}
