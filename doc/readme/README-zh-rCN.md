<div align="center">
    <div>
        <img src="../image/Anivu.svg" style="height: 210px"/>
    </div>
    <h1>🥰 AniVu</h1>
    <p>
        <a href="https://github.com/SkyD666/AniVu/releases/latest" style="text-decoration:none">
            <img src="https://img.shields.io/github/v/release/SkyD666/AniVu?display_name=release&style=for-the-badge" alt="GitHub release (latest by date)"/>
        </a>
        <a href="https://github.com/SkyD666/AniVu/releases/latest" style="text-decoration:none" >
            <img src="https://img.shields.io/github/downloads/SkyD666/AniVu/total?style=for-the-badge" alt="GitHub all downloads"/>
        </a>
        <a href="https://www.android.com/versions/nougat-7-0" style="text-decoration:none" >
            <img src="https://img.shields.io/badge/Android 7.0+-brightgreen?style=for-the-badge&logo=android&logoColor=white" alt="Support platform"/>
        </a>
        <a href="https://github.com/SkyD666/AniVu/blob/master/LICENSE" style="text-decoration:none" >
            <img src="https://img.shields.io/github/license/SkyD666/AniVu?style=for-the-badge" alt="GitHub license"/>
        </a>
        <a href="https://t.me/SkyD666Chat" style="text-decoration:none" >
            <img src="https://img.shields.io/badge/Telegram-2CA5E0?logo=telegram&logoColor=white&style=for-the-badge" alt="Telegram"/>
        </a>
        <a href="https://discord.gg/pEWEjeJTa3" style="text-decoration:none" >
            <img src="https://img.shields.io/discord/982522006819991622?color=5865F2&label=Discord&logo=discord&logoColor=white&style=for-the-badge" alt="Discord"/>
        </a>
    </p>
    <p>
        <b>AniVu</b>，一个集<b> RSS 番剧订阅与更新、比特洪流下载、视频播放</b>为一体的工具。
    </p>
    <p>
        使用 <b><a href="https://developer.android.com/topic/architecture#recommended-app-arch">MVI</a></b> 架构，完全采用 <b><a href="https://m3.material.io/">Material You</a></b> 设计风格。所有页面均使用 <b>Android View</b> 开发。
    </p>
    <p>
        <b><a href="../../README.md">English</a></b>
    </p>
</div>


## 💡主要功能

1. **订阅** RSS、**更新** RSS、**阅读** RSS
2. **下载** RSS 文章中的 **BT 种子或磁力链接**附件（enclosure 标签）
3. **播放**已下载的**视频文件**
4. 支持**倍速播放**、**双指旋转缩放视频画面**
5. 支持**深色模式**
6. ......

## 🚧待实现

1. **长按**视频画面**倍速播放**
2. **滑动**视频画面调整**音量、屏幕亮度和播放位置**
3. **自动更新 RSS 订阅**并**下载视频**
4. **自定义播放器配置**，例如：默认的画面比例、播放器使用的 Surface type 等等
5. **搜索**已有的 **RSS 订阅内容**
6. **悬浮窗播放视频**
7. **自动播放**下一个视频
8. 已下载的**文件做种**
9. **播放**手机中的**其他视频**

## 🤩应用截图

<img src="../image/zh-rCN/ic_rss_fragment.jpg" alt="ic_rss_fragment" style="zoom:80%;" /> <img src="../image/zh-rCN/ic_media_fragment.jpg" alt="ic_media_fragment" style="zoom:80%;" />
<img src="../image/zh-rCN/ic_article_fragment.jpg" alt="ic_article_fragment" style="zoom:80%;" /> <img src="../image/zh-rCN/ic_read_fragment.jpg" alt="ic_read_fragment" style="zoom:80%;" />
<img src="../image/zh-rCN/ic_read_fragment_enclosure.jpg" alt="ic_read_fragment_enclosure" style="zoom:80%;" /> <img src="../image/zh-rCN/ic_download_fragment.jpg" alt="ic_download_fragment" style="zoom:80%;" />
<img src="../image/zh-rCN/ic_about_fragment.jpg" alt="ic_about_fragment" style="zoom:80%;" />
<img src="../image/zh-rCN/ic_player_activity.png" alt="ic_player_activity" style="zoom:80%;" />

## 🛠主要技术栈

- **MVI** Architecture
- Kotlin ﻿**Coroutines and Flow**
- **Material You**
- **ViewModel**
- **Hilt**
- Media3 **ExoPlayer**
- **WorkManager**
- **DataStore**
- **Room**
- Splash Screen
- Navigation
- Coil

## ✨其他应用

<table>
<thead>
  <tr>
    <th>工具</th>
    <th>描述</th>
    <th>传送门</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td><img src="../image/Rays.svg" style="height: 100px"/></td>
    <td><b>Rays (Record All Your Stickers)</b>，一个在本地<b>记录、查找、管理表情包</b>的工具。<br/>🥰 您还在为手机中的<b>表情包太多</b>，找不到想要的表情包而苦恼吗？使用这款工具将帮助您<b>管理您存储的表情包</b>，再也不因为找不到表情包而烦恼！😋</td>
    <td><a href="https://github.com/SkyD666/Rays-Android">https://github.com/SkyD666/Rays-Android</a></td>
  </tr>
  <tr>
    <td><img src="../image/Raca.svg" style="height: 100px"/></td>
    <td><b>Raca (Record All Classic Articles)</b>，一个在本地<b>记录、查找抽象段落/评论区小作文</b>的工具。<br/>🤗 您还在为记不住小作文内容，面临<b>前面、中间、后面都忘了</b>的尴尬处境吗？使用这款工具将<b>帮助您记录您所遇到的小作文</b>，再也不因为忘记而烦恼！😋</td>
    <td><a href="https://github.com/SkyD666/Raca-Android">https://github.com/SkyD666/Raca-Android</a></td>
  </tr>
  <tr>
    <td><img src="../image/NightScreen.svg" style="height: 100px"/></td>
    <td><b>NightScreen</b>，当您在<b>夜间🌙</b>使用手机时，NightScreen 可以帮助您<b>减少屏幕亮度</b>，减少对眼睛的伤害。</td>
    <td><a href="https://github.com/SkyD666/NightScreen">https://github.com/SkyD666/NightScreen</a></td>
  </tr>
</tbody>
</table>
## 📃许可证

使用此软件代码需**遵循以下许可证协议**

[**GNU General Public License v3.0**](../../LICENSE)
