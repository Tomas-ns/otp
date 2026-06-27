package pt.isel.map

import org.osmdroid.util.GeoPoint

enum class TransportType {
    METRO, TRAIN
}

data class Station(
    val name: String,
    val stationId : String,
    val type: TransportType,
    val location: GeoPoint
)

val metroStations = listOf(
    // === LINHA AZUL ===
    Station("Reboleira", "reboleira_metro", TransportType.METRO, GeoPoint(38.7522, -9.2241)),
    Station("Amadora Este", "amadora_este_metro", TransportType.METRO, GeoPoint(38.7587, -9.2179)),
    Station("Alfornelos", "alfornelos_metro", TransportType.METRO, GeoPoint(38.7603, -9.2045)),
    Station("Pontinha", "pontinha_metro", TransportType.METRO, GeoPoint(38.7622, -9.1969)),
    Station("Carnide", "carnide_metro", TransportType.METRO, GeoPoint(38.7590, -9.1928)),
    Station("Colégio Militar/Luz", "colegio_militar_metro", TransportType.METRO, GeoPoint(38.7537, -9.1899)),
    Station("Alto dos Moinhos", "alto_dos_moinhos_metro", TransportType.METRO, GeoPoint(38.7475, -9.1800)),
    Station("Laranjeiras", "laranjeiras_metro", TransportType.METRO, GeoPoint(38.7485, -9.1725)),
    Station("Jardim Zoológico", "jardim_zoologico_metro", TransportType.METRO, GeoPoint(38.7412, -9.1664)),
    Station("Praça de Espanha", "praca_de_espanha_metro", TransportType.METRO, GeoPoint(38.7377, -9.1593)),
    Station("São Sebastião", "sao_sebastiao_metro", TransportType.METRO, GeoPoint(38.7340, -9.1536)),
    Station("Parque", "parque_metro", TransportType.METRO, GeoPoint(38.7291, -9.1501)),
    Station("Marquês de Pombal", "marques_de_pombal_metro", TransportType.METRO, GeoPoint(38.7259, -9.1500)),
    Station("Avenida", "avenida_metro", TransportType.METRO, GeoPoint(38.7193, -9.1451)),
    Station("Restauradores", "restauradores_metro", TransportType.METRO, GeoPoint(38.7155, -9.1415)),
    Station("Baixa-Chiado", "baixa_chiado_metro", TransportType.METRO, GeoPoint(38.7104, -9.1402)),
    Station("Terreiro do Paço", "terreiro_do_paco_metro", TransportType.METRO, GeoPoint(38.7072, -9.1328)),
    Station("Santa Apolónia", "santa_apolonia_metro", TransportType.METRO, GeoPoint(38.7138, -9.1225)),

    // === LINHA AMARELA ===
    Station("Odivelas", "odivelas_metro", TransportType.METRO, GeoPoint(38.7934, -9.1734)),
    Station("Senhor Roubado", "senhor_roubado_metro", TransportType.METRO, GeoPoint(38.7857, -9.1718)),
    Station("Ameixoeira", "ameixoeira_metro", TransportType.METRO, GeoPoint(38.7795, -9.1596)),
    Station("Lumiar", "lumiar_metro", TransportType.METRO, GeoPoint(38.7733, -9.1593)),
    Station("Quinta das Conchas", "quinta_das_conchas_metro", TransportType.METRO, GeoPoint(38.7675, -9.1555)),
    Station("Campo Grande", "campo_grande_metro", TransportType.METRO, GeoPoint(38.7602, -9.1578)),
    Station("Cidade Universitária", "cidade_universitaria_metro", TransportType.METRO, GeoPoint(38.7514, -9.1593)),
    Station("Entre Campos", "entrecampos_metro", TransportType.METRO, GeoPoint(38.7469, -9.1482)),
    Station("Campo Pequeno", "campo_pequeno_metro", TransportType.METRO, GeoPoint(38.7408, -9.1467)),
    Station("Saldanha", "saldanha_metro", TransportType.METRO, GeoPoint(38.7348, -9.1453)),
    Station("Picoas", "picoas_metro", TransportType.METRO, GeoPoint(38.7302, -9.1469)),
    Station("Rato", "rato_metro", TransportType.METRO, GeoPoint(38.7201, -9.1548)),

    // === LINHA VERDE ===
    Station("Telheiras", "telheiras_metro", TransportType.METRO, GeoPoint(38.7602, -9.1661)),
    Station("Alvalade", "alvalade_metro", TransportType.METRO, GeoPoint(38.7533, -9.1442)),
    Station("Roma", "roma_metro", TransportType.METRO, GeoPoint(38.7481, -9.1412)),
    Station("Areeiro", "areeiro_metro", TransportType.METRO, GeoPoint(38.7423, -9.1335)),
    Station("Alameda", "alameda_metro", TransportType.METRO, GeoPoint(38.7369, -9.1338)),
    Station("Arroios", "arroios_metro", TransportType.METRO, GeoPoint(38.7334, -9.1342)),
    Station("Anjos", "anjos_metro", TransportType.METRO, GeoPoint(38.7270, -9.1348)),
    Station("Intendente", "intendente_metro", TransportType.METRO, GeoPoint(38.7233, -9.1353)),
    Station("Martim Moniz", "martim_moniz_metro", TransportType.METRO, GeoPoint(38.7152, -9.1356)),
    Station("Rossio", "rossio_metro", TransportType.METRO, GeoPoint(38.7140, -9.1381)),
    Station("Cais do Sodré", "cais_do_sodre_metro", TransportType.METRO, GeoPoint(38.7061, -9.1451)),

    // === LINHA VERMELHA ===
    Station("Olaias", "olaias_metro", TransportType.METRO, GeoPoint(38.7402, -9.1226)),
    Station("Bela Vista", "bela_vista_metro", TransportType.METRO, GeoPoint(38.7478, -9.1177)),
    Station("Chelas", "chelas_metro", TransportType.METRO, GeoPoint(38.7547, -9.1140)),
    Station("Olivais", "olivais_metro", TransportType.METRO, GeoPoint(38.7608, -9.1118)),
    Station("Cabo Ruivo", "cabo_ruivo_metro", TransportType.METRO, GeoPoint(38.7629, -9.1051)),
    Station("Oriente", "oriente_metro", TransportType.METRO, GeoPoint(38.7678, -9.0993)),
    Station("Moscavide", "moscavide_metro", TransportType.METRO, GeoPoint(38.7749, -9.1030)),
    Station("Encarnação", "encarnacao_metro", TransportType.METRO, GeoPoint(38.7749, -9.1154)),
    Station("Aeroporto", "aeroporto_metro", TransportType.METRO, GeoPoint(38.7686, -9.1283))
)

/*
val trainStations = listOf(
// === LINHA DE CASCAIS ===
    //Station("Cais do Sodré (CP)", TransportType.TRAIN, GeoPoint(38.7056, -9.1456)),
    Station("Santos", TransportType.TRAIN, GeoPoint(38.7051, -9.1554)),
    Station("Alcântara-Mar", TransportType.TRAIN, GeoPoint(38.7027, -9.1751)),
    Station("Belém", TransportType.TRAIN, GeoPoint(38.6968, -9.1983)),
    Station("Algés", TransportType.TRAIN, GeoPoint(38.6997, -9.2306)),
    Station("Cruz Quebrada", TransportType.TRAIN, GeoPoint(38.6974, -9.2494)),
    Station("Caxias", TransportType.TRAIN, GeoPoint(38.7022, -9.2748)),
    Station("Paço de Arcos", TransportType.TRAIN, GeoPoint(38.6980, -9.2933)),
    Station("Santo Amaro", TransportType.TRAIN, GeoPoint(38.6922, -9.3101)),
    Station("Oeiras", TransportType.TRAIN, GeoPoint(38.6881, -9.3182)),
    Station("Carcavelos", TransportType.TRAIN, GeoPoint(38.6834, -9.3344)),
    Station("Parede", TransportType.TRAIN, GeoPoint(38.6874, -9.3541)),
    Station("São Pedro do Estoril", TransportType.TRAIN, GeoPoint(38.6953, -9.3702)),
    Station("São João do Estoril", TransportType.TRAIN, GeoPoint(38.7013, -9.3879)),
    Station("Estoril", TransportType.TRAIN, GeoPoint(38.7037, -9.3986)),
    Station("Monte Estoril", TransportType.TRAIN, GeoPoint(38.7020, -9.4077)),
    Station("Cascais", TransportType.TRAIN, GeoPoint(38.7007, -9.4182)),

    // === LINHA DE SINTRA ===
    Station("Rossio (CP)", TransportType.TRAIN, GeoPoint(38.7144, -9.1406)),
    Station("Campolide", TransportType.TRAIN, GeoPoint(38.7303, -9.1664)),
    Station("Benfica", TransportType.TRAIN, GeoPoint(38.7505, -9.1996)),
    Station("Santa Cruz-Damaia", TransportType.TRAIN, GeoPoint(38.7495, -9.2132)),
    Station("Reboleira (CP)", TransportType.TRAIN, GeoPoint(38.7522, -9.2241)),
    Station("Amadora", TransportType.TRAIN, GeoPoint(38.7584, -9.2378)),
    Station("Queluz-Belas", TransportType.TRAIN, GeoPoint(38.7583, -9.2568)),
    Station("Monte Abraão", TransportType.TRAIN, GeoPoint(38.7610, -9.2662)),
    Station("Massamá-Barcarena", TransportType.TRAIN, GeoPoint(38.7628, -9.2801)),
    Station("Agualva-Cacém", TransportType.TRAIN, GeoPoint(38.7663, -9.2987)),
    Station("Rio de Mouro", TransportType.TRAIN, GeoPoint(38.7758, -9.3204)),
    Station("Mercês", TransportType.TRAIN, GeoPoint(38.7831, -9.3361)),
    Station("Algueirão-Mem Martins", TransportType.TRAIN, GeoPoint(38.7946, -9.3421)),
    Station("Portela de Sintra", TransportType.TRAIN, GeoPoint(38.8016, -9.3780)),
    Station("Sintra", TransportType.TRAIN, GeoPoint(38.7985, -9.3862)),

    // === LINHA DE CINTURA / AZAMBUJA (ZONA URBANA) ===
    Station("Sete Rios", TransportType.TRAIN, GeoPoint(38.7397, -9.1665)),
    Station("Entrecampos (CP)", TransportType.TRAIN, GeoPoint(38.7441, -9.1481)),
    Station("Roma-Areeiro", TransportType.TRAIN, GeoPoint(38.7456, -9.1352)),
    Station("Braço de Prata", TransportType.TRAIN, GeoPoint(38.7440, -9.1000)),
    Station("Oriente (CP)", TransportType.TRAIN, GeoPoint(38.7678, -9.0993)),
    Station("Moscavide (CP)", TransportType.TRAIN, GeoPoint(38.7766, -9.0996)),
    Station("Sacavém", TransportType.TRAIN, GeoPoint(38.7937, -9.1026)),
    Station("Santa Apolónia (CP)", TransportType.TRAIN, GeoPoint(38.7144, -9.1221)),

    // === FERTAGUS (MARGEM SUL) ===
    Station("Pragal", TransportType.TRAIN, GeoPoint(38.6651, -9.1740)),
    Station("Corroios", TransportType.TRAIN, GeoPoint(38.6292, -9.1537)),
    Station("Foros de Amora", TransportType.TRAIN, GeoPoint(38.6148, -9.1306)),
    Station("Fogueteiro", TransportType.TRAIN, GeoPoint(38.6186, -9.0988)),
    Station("Coina", TransportType.TRAIN, GeoPoint(38.6044, -9.0553)),
    Station("Penalva", TransportType.TRAIN, GeoPoint(38.5997, -9.0063)),
    Station("Pinhal Novo", TransportType.TRAIN, GeoPoint(38.6322, -8.9126)),
    Station("Palmela", TransportType.TRAIN, GeoPoint(38.5833, -8.8897)),
    Station("Setúbal", TransportType.TRAIN, GeoPoint(38.5283, -8.8837))
)

 */