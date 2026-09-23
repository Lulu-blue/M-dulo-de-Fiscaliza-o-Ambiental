export interface Usuario {
  id?: string;
  nome: string;
  cpf: string;
  matricula: string;
  cargo: 'GESTOR' | 'FISCAL';
  ativo?: boolean;
}

export interface Contribuinte {
  id?: string;
  nome: string;
  cpfCnpj: string;
  rua?: string;
  numero?: string;
  bairro?: string;
  municipio?: string;
  cep?: string;
}

export interface Imovel {
  id?: string;
  inscricao?: string;
  codigoReduzido?: string;
  rua?: string;
  numero?: string;
  bairro?: string;
  cep?: string;
  contribuinte?: Contribuinte;
}

export interface Demanda {
  id?: string;
  titulo: string;
  descricao: string;
  status: 'PENDENTE' | 'EM_ANDAMENTO' | 'CONCLUIDO';
  gestorCriador?: Usuario;
  fiscalAtribuido?: Usuario;
  dataCriacao?: string;
  dataConclusao?: string;
  localizacao?: string;
  urgencia?: 'BAIXA' | 'MEDIA' | 'ALTA';
  podeEditar?: boolean;
  anexos?: Anexo[];
}

export interface AutoFiscalizacao {
  id?: string;
  numeroSequencial?: number;
  ano?: number;
  demanda: Demanda;
  criador: Usuario;
  contribuinte: Contribuinte;
  imovel: Imovel;
  processoAdministrativo: string;
  irregularidadesConstatadas: string;
  prazoDefesa: number;
  dadosVistoria?: string;
  pontosProdutividade?: number;
  dataGeracao?: string;
  providencias?: string;
  dispositivosLegais?: string;
  penalidades?: string;
  temTestemunhas?: boolean;
  nomeTestemunha1?: string;
  cpfTestemunha1?: string;
  nomeTestemunha2?: string;
  cpfTestemunha2?: string;
  documentoAssinadoNome?: string;
  documentoAssinadoContentType?: string;
  documentoAssinadoEnviadoEm?: string;
}

export interface RelatorioImagem {
  id?: string;
  imagemBase64: string;
  legenda?: string;
  ordem?: number;
}

export interface Relatorio {
  id?: string;
  numeroSequencial?: number;
  ano?: number;
  demanda: Demanda;
  imovel: Imovel;
  dataHoraVistoria: string;
  assunto?: string;
  atendimento?: string;
  processoAdministrativo?: string;
  textoVistoria?: string;
  imagens?: RelatorioImagem[];
  pontosProdutividadeBase?: number;
  dataGeracao?: string;
  fiscais?: Usuario[];
  criador?: Usuario;
  documentoAssinadoNome?: string;
  documentoAssinadoContentType?: string;
  documentoAssinadoEnviadoEm?: string;
}

export interface Anexo {
  id?: string;
  demanda?: Demanda;
  nomeArquivo: string;
  hashSha256: string;
  tamanhoBytes: number;
  dataUpload?: string;
  contentType?: string;
  enviadoPor?: Usuario;
}

export interface DocumentoPontuacao {
  tipo: 'AUTO' | 'RELATORIO';
  id: string;
  numeroSequencial?: number;
  ano?: number;
  pontos: number;
  demandaTitulo?: string;
  dataGeracao?: string;
}

export interface RankingFiscal {
  fiscal: Usuario;
  totalPontos: number;
  documentos: DocumentoPontuacao[];
}

export interface PontuacaoDocumento {
  tipo: 'AUTO' | 'RELATORIO';
  id: string;
  numeroSequencial?: number;
  ano?: number;
  pontos: number;
  demandaTitulo?: string;
  dataAssinatura: string;
}

export interface PontuacaoDiaria {
  dia: string;
  pontos: number;
}

export interface MinhaPontuacaoResposta {
  totalPontos: number;
  porDia: PontuacaoDiaria[];
  documentos: PontuacaoDocumento[];
}

export interface LoginRequest {
  cpf: string;
  senha?: string;
}

export interface LoginResponse {
  token: string;
  nome: string;
  cargo: 'GESTOR' | 'FISCAL';
}
