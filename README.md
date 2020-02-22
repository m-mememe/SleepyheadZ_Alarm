# Alarm for sleepyheads

どうしても朝起きれない人に向けたアラーム。  
朝が苦手に人がよくやる複数のアラームをセットするのを一括管理できるようにする。  

![キャプチャ](https://user-images.githubusercontent.com/51912962/75100678-4084c880-5614-11ea-89cb-ea8e589273e9.PNG)

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
アラームごとの着信音設定
開始時間と終了時間の同時変更
```

### PlayMusicActivity
```
AlarmManagerによる呼び出し
音楽（アラーム）の再生
livedoorの天気APIを利用してアラーム鳴動時の天気表示
時間帯と天気により背景色変更
```

## Requirements
- Realm

## Test environment
- Nexus 5X API28 (AVD)
- Xperia XZs API26 (Real machine)

## ToDoList
```  
アイコンの作成・変更
アニメーションの追加
一様分布以外にアラームを設定できるようにする（そのためにデータベースに音楽の種類と分布の選択を追加する必要あり）
Preferenceを使用して一般的な設定をカスタマイズ可能にする
```
