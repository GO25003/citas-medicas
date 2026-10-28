export type EstadoCita = 'PENDIENTE' | 'CONFIRMADA' | 'CANCELADA'
export interface EspecialidadResponse { idEspecialidad:number; nombre:string; descripcion:string|null }
export interface EspecialidadRequest { nombre:string; descripcion:string|null }
export interface MedicoResponse { idMedico:number; nombre:string; apellido:string; email:string; telefono:string|null; numeroColegiado:string; especialidades:EspecialidadResponse[] }
export interface MedicoRequest { nombre:string; apellido:string; email:string; telefono:string|null; numeroColegiado:string; idsEspecialidades:number[] }
export interface PacienteResponse { idPaciente:number; nombre:string; apellido:string; email:string; telefono:string|null; fechaNacimiento:string }
export interface PacienteRequest { nombre:string; apellido:string; email:string; telefono:string|null; fechaNacimiento:string }
export interface DisponibilidadResponse { idDisponibilidad:number; medicoId:number; fecha:string; horaInicio:string; horaFin:string; disponible:boolean }
export interface DisponibilidadRequest { medicoId:number; fecha:string; horaInicio:string; horaFin:string; disponible:boolean }
export interface CitaResponse { idCita:number; idMedico:number; nombreMedico:string; idPaciente:number; nombrePaciente:string; idEspecialidad:number; nombreEspecialidad:string; fechaHoraInicio:string; fechaHoraFin:string; duracionMinutos:number; motivo:string|null; estado:EstadoCita }
export interface CitaRequest { idMedico:number; idPaciente:number; idEspecialidad:number; fechaHoraInicio:string; duracionMinutos:number; motivo:string|null }
export interface ApiError { timestamp?:string; status?:number; error?:string; message?:string; path?:string }
