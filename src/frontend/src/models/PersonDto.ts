export default class PersonDto {
  id?: number
  name?: string
  istId?: string
  email?:string
  type?: string

  constructor(obj?: Partial<PersonDto>) {
    Object.assign(this, obj)
  }
}
