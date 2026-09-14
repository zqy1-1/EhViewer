<p align="right">
  <strong>English</strong>
  <span> | </span>
  <a href="/docs/README/zh-cn.md">
  简体中文
  </a>
  <span> | </span>
  <a href="/docs/README/zh-tw.md">
  正體中文
  </a>
  <span> | </span>
  <a href="/docs/README/ja.md">
  日本語
  </a>
</p>

<h1 align="center">
  <img src="https://github.com/FooIbar/EhViewer-art/blob/master/launcher_icon-web.svg" width="200" alt="EhViewer">
  <br>EhViewer<br>
</h1>

<p align="center">
  <a href="https://github.com/FooIbar/EhViewer/actions/workflows/ci.yml">
    <img src="https://github.com/FooIbar/EhViewer/actions/workflows/ci.yml/badge.svg" alt="Github Actions">
  </a>
  <a href="/LICENSE">
    <img src="https://img.shields.io/github/license/FooIbar/EhViewer" alt="LICENSE">
  </a>
  <a href="https://www.codefactor.io/repository/github/FooIbar/EhViewer">
    <img src="https://www.codefactor.io/repository/github/FooIbar/EhViewer/badge" alt="CodeFactor">
  </a>
  <a href="https://github.com/FooIbar/EhViewer/releases">
    <img src="https://img.shields.io/github/v/release/FooIbar/EhViewer" alt="Release">
  </a>
  <a href="https://github.com/FooIbar/EhViewer/issues">
    <img src="https://img.shields.io/github/issues/FooIbar/EhViewer" alt="Issues">
  </a>
</p>

<div align="center">
  <h3>
    <a href="#description">
    Description
    </a>
    <span> | </span>
    <a href="#download">
    Download
    </a>
    <span> | </span>
    <a href="#screenshot">
    Screenshot
    </a>
    <span> | </span>
    <a href="#thanks">
    Thanks
    </a>
    <span> | </span>
    <a href="#license">
    License
    </a>
  </h3>
</div>

# Description
fork自FooIbar/EhViewer的1.14.6版本，然后在此基础上把cookie登录加上了，然后加了绿E的网络方案，cookie登录可以绕开cloudflare验证了，不过还是需要开vpn登录，看漫画可以直连。安装不会覆盖原有的彩E，可以共存。做的糙了点,主要还是自用，把cookie粘贴进去就行了。

cookie的大体格式就是：

ipb_member_id:123456

ipb_pass_hash:asdsasdadasdasdasd

igneous:null

弄的时候踩了不少坑，FooIbar/EhViewer的最新的源码把直连e站的代码部分给删了，弄得一开始我明明加好了cookie登录，却没法直连。

A modern EhViewer fork dedicated to high-performance

with [Material Design 3](https://m3.material.io/)
and [Dynamic Color](https://m3.material.io/styles/color/dynamic-color/overview) Support

# Download

| Flavor      | Minimum Android Version | Notes                          |
|-------------|-------------------------|--------------------------------|
| Default     | 8.0                     | Full support                   |
| Marshmallow | 6.0*                    | Limited support, no guarantees |

*Devices running Android 6 need to have the [ISRG Root X1](https://letsencrypt.org/certs/isrgrootx1.pem) certificate installed

<a href="https://github.com/FooIbar/EhViewer/releases">
<img alt="Get it on GitHub" src="https://github.com/FooIbar/EhViewer-art/blob/master/get-it-on-github.svg" width="200px"/>
</a>

# Screenshot

![screenshots-01](https://github.com/FooIbar/EhViewer-art/blob/master/screenshots-01.webp)
![screenshots-02](https://github.com/FooIbar/EhViewer-art/blob/master/screenshots-02.webp)

# Thanks

Here is the libraries

- [Arrow](https://arrow-kt.io/)
- [AOSP & AndroidX](https://source.android.com/)
- [Kotlin & KotlinX](https://kotlinlang.org/)
- [Material Icons](https://github.com/google/material-design-icons)
- [Ktor](https://ktor.io/)
- [Coil](https://coil-kt.github.io/coil/)
- [Compose Destinations](https://composedestinations.rafaelcosta.xyz/)
- [libarchive](https://www.libarchive.org/)

# License

    Copyright 2014-2019 Hippo Seven
    Copyright 2020-2022 NekoInverter
    Copyright 2022-2023 Tarsin Norbin
    Copyright 2023-2024 Foolbar

    EhViewer is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

    EhViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along with EhViewer. If not, see <https://www.gnu.org/licenses/>.
