NULLチェックはLet?.{　}?:　でもいいが､ネストが深くなりそうなら異常ケースは　早期リターン


.gitignore
.ida/vcs.xml, misc.xmlはGitに入れないでおく｡

最後のラムダ文は)の後ろにカンマなしでくっつける｡

#調べたこと
##Koin＋Room
Viewmodelを呼び出せる｡
Activityでは　
       val myViewmodel by viewModel<MainViewModel>()
Fragementでは 
       val myViewmodel by sharedViewModel<MainViewModel>()

Applicaitonクラスを継承したMyApplicaitonクラスで
       
       startKoin {
            androidContext(this@MyApplication)
            modules(myModule)
        }
       private val myModule:Module = module {
        single { //シングルトンでデータベースクラスをインスタンス化しておく｡
            Room.databaseBuilder(androidContext(),MyDataBase::class.java,"myDatabase.db").build()
                //　DAOはFactoryとしておく｡
        factory { get<MyDataBase>().myDao() }
        viewModel { MainViewModel(get()) }　　//　こうしておくと､ViewModelが初期化されたときに一致する引数がFactoryから自動的に渡される

##FlexboxLayout
ViewGroup
W3cのFlexboxと一緒｡
wrapとすると折り返してくれる｡
アイテムをたくさん追加するとメモリを圧迫するので､Recylcer viewをつかう｡
普通にRecycleviewを作って､LayoutmanagerをFlexboxLayoutManagerにするとよい｡

XMLの設定だとこんなもの


    <com.google.android.flexbox.FlexboxLayout android:id="@+id/detail_tag2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_constraintTop_toBottomOf="@id/detail_tag" android:layout_marginTop="8dp"
        app:layout_constraintStart_toStartOf="parent"
        app:flexWrap="wrap"
        app:alignItems="flex_start"
        app:alignContent="flex_start"
        />
