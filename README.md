# Alarm for sleepyheads

どうしても朝起きれない人に向けたアラーム。  
朝が苦手に人がよくやる複数のアラームをセットするのを一括管理できるようにする。  

![sleepyheadz_alarm_icon_for_Git](https://user-images.githubusercontent.com/51912962/75628063-d45c2300-5c18-11ea-915b-f7f589aaf3b5.png)
![git_readme](https://user-images.githubusercontent.com/51912962/75628036-ad9dec80-5c18-11ea-9513-1ef3c743fb59.png)

## Overview

特徴しては複数のアラームを一括で管理する点。  
アラームは開始時間と終了時間、鳴らす回数の3つを設定する。  
開始時間と終了時間、鳴らす回数それぞれをStart, Stop, Countとして、以下のdigitを計算する。 
 
digit = (Stop - Start) / (Count - 1) 
 
開始時間からdigit毎にアラームを鳴らす。  
例として以下のように動作する。 

e.g.)  
Start: 7:00, Stop: 7:20, Count: 6  
↓  
digit: 4 minutes  
Ring at: 7:00, 7:04, 7:08, 7:12, 7:16, 7:20  

## Detail

各アクティビティでしてる・できること

### MainActivity
```
起動時に全アラームのセット・リセット
RecyclerView上のスイッチ切り替えによるアラームのセット・リセット
コンテキストメニューでのアラーム削除
```

### TimerMenuActivity
```
アラームの追加・削除（セット・リセットも同時に行う）
アラームごとの音楽設定
開始時間と終了時間の同時変更
```

### PlayMusicActivity
```
AlarmManagerによる呼び出し
指定した音楽（アラーム）の再生
livedoorの天気APIを利用してアラーム鳴動時に天気を画像で表示
天気画像をタップすることで天気の詳細をgoogle検索
時間帯と天気により背景色変更
```

### SettingActivity
```
以下の設定
・デフォルトの開始時間と終了時間の時間差
・デフォルトのカウント数
・ソートキーの選択
・表示する天気の地域（関東圏）
```

## Requirements
- Realm

## Test environment
- Nexus 5X API28 (AVD)
- Xperia XZs API26 (Real machine)

## GooglePlayStore
http://hoge.hoge
